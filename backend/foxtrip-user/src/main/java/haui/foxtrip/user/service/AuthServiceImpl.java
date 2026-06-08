package haui.foxtrip.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.security.AuthoritiesConstants;
import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.domain.enums.Role;
import haui.foxtrip.user.repository.UserRepository;
import haui.foxtrip.user.security.TokenProvider;
import haui.foxtrip.user.service.dto.request.*;
import haui.foxtrip.user.service.dto.response.AuthResponse;
import haui.foxtrip.user.util.EmailNormalizeUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import haui.foxtrip.user.util.GeneratePasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthServiceImpl implements AuthService {

    private static final String SESSION_CACHE = "user-session";

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final CacheManager cacheManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthEmailService authEmailService;

    @Value("${application.auth.google.client-id}")
    private String googleClientId;

    private static final String DEFAULT_AVATAR_URL = "https://res.cloudinary.com/do1ill8ba/image/upload/v1775034651/default_image.png";

    // ── only mobile ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public String register(RegisterRequest request) {
        String email = EmailNormalizeUtil.normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), "Email đã được sử dụng.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), "Số điện thoại đã được sử dụng.");
        }
        boolean shouldSendOtp = request.getSendOtp() == null || request.getSendOtp();
        User user = new User();
        user.setEmail(email);
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        if (shouldSendOtp) {
            authEmailService.sendOtpRegister(email);
            return "OTP đã được gửi vào email.";
        }
        return "Đăng ký thành công. Hãy xác thực tài khoản.";
    }

    // ── only mobile ──────────────────────────────────────────────────────────
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = EmailNormalizeUtil.normalize(request.getEmail());
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người dùng."));

        String tempPassword = GeneratePasswordUtil.generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        getSessionCache().evict(user.getId().toString());
        authEmailService.sendPasswordResetEmail(email, tempPassword);
    }

    // ── only mobile ──────────────────────────────────────────────────────────
    @Override
    public AuthResponse login(LoginRequest request) {
        String email = EmailNormalizeUtil.normalize(request.getEmail());
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Không tìm thấy người dùng.");
        }
        User user = userOpt.get();
        if (user.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Tài khoản đã bị vô hiệu hóa.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Email hoặc mật khẩu không đúng.");
        }
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());
        String googleId = payload.getSubject();
        String email = EmailNormalizeUtil.normalize(payload.getEmail());
        String name = payload.get("name") != null ? payload.get("name").toString() : null;
        String picture = payload.get("picture") != null ? payload.get("picture").toString() : null;
        User user = upsertGoogleUser(googleId, email, name, picture);
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshAndLogoutTokenRequest request) {
        try {
            Jwt jwt = decodeRefreshTokenOrThrow(request.getRefreshToken());
            UUID userId = UUID.fromString(jwt.getSubject());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người dùng."));
            String sessionId = jwt.getClaimAsString("sid");
            if (sessionId == null || sessionId.isBlank()) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Refresh token không hợp lệ.");
            }
            if (user.getDeletedAt() != null) {
                getSessionCache().evict(userId.toString());
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Tài khoản đã bị vô hiệu hóa.");
            }
            return buildAuthResponse(user, sessionId);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Refresh token không hợp lệ.");
        }
    }

    @Override
    public void logout(RefreshAndLogoutTokenRequest request) {
        try {
            Jwt jwt = decodeRefreshTokenOrThrow(request.getRefreshToken());
            String subject = jwt.getSubject();
            UUID userId = UUID.fromString(subject);
            getSessionCache().evict(userId.toString());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Refresh token không hợp lệ.");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String sessionId = ensureSession(user.getId());
        return buildAuthResponse(user, sessionId);
    }

    private AuthResponse buildAuthResponse(User user, String sessionId) {
        String subject = user.getId().toString();
        List<String> authorities = resolveAuthorities(user);
        String accessToken = tokenProvider.createAccessToken(
                subject, user.getId(), authorities, sessionId, Boolean.TRUE.equals(user.getIsVerified()));
        String refreshToken = tokenProvider.createRefreshToken(subject, sessionId);
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(tokenProvider.getAccessTokenValiditySeconds());
        response.setRole((user.getRole() == null ? Role.USER : user.getRole()).name());
        return response;
    }

    private List<String> resolveAuthorities(User user) {
        Role role = user.getRole();
        if (role == null) {
            return List.of(AuthoritiesConstants.USER);
        }
        return switch (role) {
            case SUPER_ADMIN -> List.of(AuthoritiesConstants.SUPER_ADMIN);
            case ADMIN -> List.of(AuthoritiesConstants.ADMIN);
            case GUIDE -> List.of(AuthoritiesConstants.GUIDE);
            default -> List.of(AuthoritiesConstants.USER);
        };
    }

    private String ensureSession(UUID userId) {
        String sessionId = UUID.randomUUID().toString();
        getSessionCache().put(userId.toString(), sessionId);
        return sessionId;
    }

    private Cache getSessionCache() {
        Cache cache = cacheManager.getCache(SESSION_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Không tồn tại cache: " + SESSION_CACHE);
        }
        return cache;
    }

    private Jwt decodeRefreshTokenOrThrow(String refreshToken) {
        Jwt jwt = jwtDecoder.decode(refreshToken);
        if (!"refresh".equals(jwt.getClaimAsString("token_type"))) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Refresh token không hợp lệ.");
        }
        return jwt;
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(googleClientId))
                    .build();
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Google token không hợp lệ.");
            }
            return token.getPayload();
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Google token không hợp lệ.");
        }
    }

    private User upsertGoogleUser(String googleId, String email, String name, String picture) {
        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingByGoogleId.isPresent()) {
            User user = existingByGoogleId.get();
            if (user.getDeletedAt() != null) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Tài khoản đã bị vô hiệu hóa.");
            }
            if (!email.equals(user.getEmail())) {
                user.setEmail(email);
            }
            if (!Boolean.TRUE.equals(user.getIsVerified())) {
                user.setIsVerified(true);
            }
            if (picture != null && isAvatarEmptyOrDefault(user.getAvatarUrl())) {
                user.setAvatarUrl(picture);
            }
            return userRepository.save(user);
        }
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            if (user.getDeletedAt() != null) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Tài khoản đã bị vô hiệu hóa.");
            }
            user.setGoogleId(googleId);
            if (!Boolean.TRUE.equals(user.getIsVerified())) {
                user.setIsVerified(true);
            }
            if (picture != null && isAvatarEmptyOrDefault(user.getAvatarUrl())) {
                user.setAvatarUrl(picture);
            }
            return userRepository.save(user);
        }
        User user = new User();
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setUsername((name == null || name.isBlank()) ? email : name);
        user.setIsVerified(true);
        if (picture != null && isAvatarEmptyOrDefault(user.getAvatarUrl())) {
            user.setAvatarUrl(picture);
        }
        return userRepository.save(user);
    }

    private boolean isAvatarEmptyOrDefault(String avatarUrl) {
        return avatarUrl == null || avatarUrl.isBlank() || DEFAULT_AVATAR_URL.equals(avatarUrl);
    }
}
