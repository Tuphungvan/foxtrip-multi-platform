package haui.foxtrip.user.service;

import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.repository.UserRepository;
import haui.foxtrip.user.util.EmailNormalizeUtil;
import haui.foxtrip.user.util.EmailTemplateUtil;
import haui.foxtrip.user.util.OtpFormatUtil;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthEmailServiceImpl implements AuthEmailService {

    private static final String OTP_CACHE = "email-otp";

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final TaskExecutor taskExecutor;

    public AuthEmailServiceImpl(
            JavaMailSender mailSender,
            UserRepository userRepository,
            CacheManager cacheManager,
            @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.taskExecutor = taskExecutor;
    }

    @Value("${spring.mail.from:${spring.mail.username:}}")
    private String fromAddress;

    // ── after register ──────────────────────────────────────────────────────────
    @Override
    public void sendOtpRegister(String email) {
        String otp = OtpFormatUtil.generateOtp();
        getCache(OTP_CACHE).put(email, otp);
        sendOtpEmail(email, otp);
        log.info("Sent OTP to {}", email);
    }

    // ── after register ──────────────────────────────────────────────────────────
    @Override
    public void verifyEmail(String email, String otp) {
        String normalizedEmail = EmailNormalizeUtil.normalize(email);
        String storedOtp = getCache(OTP_CACHE).get(normalizedEmail, String.class);
        if (storedOtp == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "OTP đã hết hạn.");
        }
        if (!storedOtp.equals(otp)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "OTP không hợp lệ.");
        }
        User user = userRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người dùng."));
        user.setIsVerified(true);
        userRepository.save(user);
        getCache(OTP_CACHE).evict(normalizedEmail);
    }

    // ── after register ──────────────────────────────────────────────────────────
    @Override
    public boolean resendOtpIfNotVerified(String email) {
        User user = findActiveUserByEmail(email);
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return false;
        }
        sendOtpToUser(user);
        return true;
    }

    // ── have account ──────────────────────────────────────────────────────────
    @Override
    public boolean resendOtpForCurrentUser() {
        User user = getCurrentUser();
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return false;
        }
        sendOtpToUser(user);
        return true;
    }

    // ── have account ──────────────────────────────────────────────────────────
    @Override
    public void verifyOtpForCurrentUser(String otp) {
        User user = getCurrentUser();
        verifyEmail(user.getEmail(), otp);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String tempPassword) {
        if (email == null || email.isBlank()) {
            return;
        }
        String html = EmailTemplateUtil.render(
                "util/templates/auth-temp-password-email.html",
                Map.of("TEMP_PASSWORD", tempPassword));
        sendHtmlEmail(email, "Foxtrip - Mật khẩu tạm thời", html);
    }

    @Override
    @Async
    public void sendGuideAccountEmail(String email, String password) {
        if (email == null || email.isBlank()) {
            return;
        }
        String html = EmailTemplateUtil.render(
                "util/templates/guide-creation-email.html",
                Map.of("EMAIL", email, "PASSWORD", password));
        sendHtmlEmail(email, "Foxtrip - Thông tin tài khoản Guide", html);
    }

    private void sendOtpEmail(String email, String otp) {
        String html = EmailTemplateUtil.render("util/templates/auth-otp-email.html", Map.of("OTP", otp));
        taskExecutor.execute(() -> sendHtmlEmail(email, "Foxtrip - Xác thực email", html));
    }

    private User findActiveUserByEmail(String email) {
        String normalizedEmail = EmailNormalizeUtil.normalize(email);
        return userRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người dùng."));
    }

    private void sendOtpToUser(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Email là bắt buộc.");
        }
        String normalizedEmail = EmailNormalizeUtil.normalize(email);
        String otp = OtpFormatUtil.generateOtp();
        getCache(OTP_CACHE).put(normalizedEmail, otp);
        sendOtpEmail(normalizedEmail, otp);
        log.info("Sent OTP to {}", normalizedEmail);
    }

    private Cache getCache(String name) {
        Cache cache = cacheManager.getCache(name);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured: " + name);
        }
        return cache;
    }

    private User getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa xác thực."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người dùng."));
        if (user.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Tài khoản đã bị vô hiệu hóa.");
        }
        return user;
    }

    private void sendHtmlEmail(String email, String subject, String html) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Unable to send auth email subject={} to {}: {}", subject, email, ex.getMessage());
        }
    }
}

