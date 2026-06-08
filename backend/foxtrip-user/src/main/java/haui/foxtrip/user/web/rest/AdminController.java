package haui.foxtrip.user.web.rest;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.user.service.UserService;
import haui.foxtrip.user.service.dto.request.CreateAdminRequest;
import haui.foxtrip.user.service.dto.request.CreateGuideRequest;
import haui.foxtrip.user.service.dto.request.UpdateStaffProfileRequest;
import haui.foxtrip.user.service.dto.response.UserDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getMe() {
        UserDetailResponse data = userService.getMe();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", data));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody UpdateStaffProfileRequest request) {
        userService.updateStaffProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageData<UserDetailResponse>>> searchUsers(
            @RequestParam(value = "email", required = false) String email,
            Pageable pageable) {
        PageData<UserDetailResponse> data = userService.searchUsers(email, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", data));
    }

    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable("userId") UUID userId) {
        userService.lockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Khóa người dùng thành công"));
    }

    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable("userId") UUID userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Mở khóa người dùng thành công"));
    }

    @PostMapping("/users/guides")
    public ResponseEntity<ApiResponse<Void>> createGuide(@Valid @RequestBody CreateGuideRequest request) {
        String message = userService.createGuide(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/users/admins")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        String message = userService.createAdmin(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
