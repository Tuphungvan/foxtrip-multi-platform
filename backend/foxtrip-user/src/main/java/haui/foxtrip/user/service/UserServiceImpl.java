package haui.foxtrip.user.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.domain.enums.Role;
import haui.foxtrip.user.repository.UserRepository;
import haui.foxtrip.user.service.dto.request.*;
import haui.foxtrip.user.service.dto.response.*;
import haui.foxtrip.user.service.mapper.UserMapper;
import haui.foxtrip.user.util.GeneratePasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class UserServiceImpl implements UserService {

    private static final String SESSION_CACHE = "user-session";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthEmailService authEmailService;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getMe() {
        User user = getCurrentUser();
        return userMapper.toUserDetailResDTO(user);
    }

    @Override
    @Transactional
    public void updateUserProfile(UpdateUserProfileRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.USER) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(),
                    "Chỉ người dùng thường mới có thể sử dụng chức năng này");
        }
        validatePhoneNumberUniqueness(request.getPhoneNumber(), user.getPhoneNumber());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateUserFromDTO(request, user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateStaffProfile(UpdateStaffProfileRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.GUIDE && user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền sử dụng chức năng này");
        }
        validatePhoneNumberUniqueness(request.getPhoneNumber(), user.getPhoneNumber());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateStaffFromDTO(request, user);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<UserDetailResponse> searchUsers(String email, Pageable pageable) {
        User currentUser = getCurrentUser();
        List<Role> allowedRoles;
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            allowedRoles = Arrays.asList(Role.USER, Role.GUIDE, Role.ADMIN);
        } else if (currentUser.getRole() == Role.ADMIN) {
            allowedRoles = Arrays.asList(Role.USER, Role.GUIDE);
        } else {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền truy cập");
        }
        Page<User> userPage = userRepository.searchUsersByRolesAndEmail(allowedRoles, email, pageable);
        return PageData.of(userPage.map(userMapper::toUserDetailResDTO));
    }

    @Override
    @Transactional
    public void lockUser(UUID userId) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Người dùng cần khóa không tồn tại"));
        if (targetUser.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Người dùng đã bị khóa");
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // Có quyền khóa tất cả người dùng
        } else if (currentUser.getRole() == Role.ADMIN) {
            if (targetUser.getRole() != Role.USER && targetUser.getRole() != Role.GUIDE) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Admin chỉ có thể khóa USER và GUIDE");
            }
        } else {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền khóa người dùng");
        }
        targetUser.setDeletedAt(Instant.now());
        userRepository.save(targetUser);
        getSessionCache().evict(userId.toString());
    }

    @Override
    @Transactional
    public void unlockUser(UUID userId) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Người dùng cần mở khóa không tồn tại"));
        if (targetUser.getDeletedAt() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Người dùng chưa bị khóa");
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // Có quyền mở khóa tất cả người dùng
        } else if (currentUser.getRole() == Role.ADMIN) {
            if (targetUser.getRole() != Role.USER && targetUser.getRole() != Role.GUIDE) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Admin chỉ có thể mở khóa USER và GUIDE");
            }
        } else {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền mở khóa người dùng");
        }
        targetUser.setDeletedAt(null);
        userRepository.save(targetUser);
    }

    @Override
    @Transactional
    public String createGuide(CreateGuideRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền tạo hướng dẫn viên");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Email đã tồn tại");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số điện thoại đã tồn tại");
        }
        User guide = userMapper.toUserFromCreateGuideDTO(request);
        guide.setRole(Role.GUIDE);
        guide.setIsVerified(true);
        String randomPassword = GeneratePasswordUtil.generateTempPassword();
        guide.setPasswordHash(passwordEncoder.encode(randomPassword));
        userRepository.save(guide);
        authEmailService.sendGuideAccountEmail(guide.getEmail(), randomPassword);
        return "Tạo hướng dẫn viên thành công";
    }

    @Override
    @Transactional
    public String createAdmin(CreateAdminRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Chỉ SUPER_ADMIN mới có quyền tạo ADMIN");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Email đã tồn tại");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số điện thoại đã tồn tại");
        }
        User admin = userMapper.toUserFromCreateAdminDTO(request);
        admin.setRole(Role.ADMIN);
        admin.setIsVerified(true);
        String randomPassword = GeneratePasswordUtil.generateTempPassword();
        admin.setPasswordHash(passwordEncoder.encode(randomPassword));
        userRepository.save(admin);
        authEmailService.sendGuideAccountEmail(admin.getEmail(), randomPassword);
        return "Tạo quản trị viên thành công";
    }

    private User getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(),
                        "Không thể xác định người dùng hiện tại"));
        return userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Người dùng không tồn tại"));
    }

    private void validatePhoneNumberUniqueness(String newPhoneNumber, String currentPhoneNumber) {
        if (newPhoneNumber != null && !newPhoneNumber.isBlank()
                && !newPhoneNumber.equals(currentPhoneNumber)
                && userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số điện thoại đã tồn tại");
        }
    }

    private Cache getSessionCache() {
        Cache cache = cacheManager.getCache(SESSION_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Không tồn tại cache: " + SESSION_CACHE);
        }
        return cache;
    }
}
