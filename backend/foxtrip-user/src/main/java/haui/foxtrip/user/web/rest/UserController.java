package haui.foxtrip.user.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.user.service.AuthEmailService;
import haui.foxtrip.user.service.UserService;
import haui.foxtrip.user.service.dto.request.UpdateUserProfileRequest;
import haui.foxtrip.user.service.dto.request.VerifyOtpRequest;
import haui.foxtrip.user.service.dto.response.UserDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class UserController {

    private final UserService userService;
    private final AuthEmailService authEmailService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getMe() {
        UserDetailResponse data = userService.getMe();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", data));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        userService.updateUserProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công"));
    }

    @PostMapping("/me/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        authEmailService.verifyOtpForCurrentUser(request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công"));
    }

    @PostMapping("/me/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp() {
        boolean sent = authEmailService.resendOtpForCurrentUser();
        if (!sent) {
            return ResponseEntity.ok(ApiResponse.success("Email đã được xác thực"));
        }
        return ResponseEntity.ok(ApiResponse.success("OTP đã được gửi lại vào email"));
    }
}
