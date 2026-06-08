package haui.foxtrip.user.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.user.service.AuthEmailService;
import haui.foxtrip.user.service.AuthService;
import haui.foxtrip.user.service.dto.request.*;
import haui.foxtrip.user.service.dto.response.AuthResponse;
import haui.foxtrip.user.util.EmailNormalizeUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthEmailService authEmailService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        String email = EmailNormalizeUtil.normalize(request.getEmail());
        authEmailService.verifyEmail(email, request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        String email = EmailNormalizeUtil.normalize(request.getEmail());
        boolean sent = authEmailService.resendOtpIfNotVerified(email);
        if (!sent) {
            return ResponseEntity.ok(ApiResponse.success("Email đã được xác thực."));
        }
        return ResponseEntity.ok(ApiResponse.success("OTP đã được gửi lại vào email."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Mật khẩu tạm thời đã được gửi vào email."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công.", authService.login(request)));
    }

    @PostMapping("/login/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công.", authService.loginWithGoogle(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshAndLogoutTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Làm mới phiên thành công.", authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshAndLogoutTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công."));
    }
}
