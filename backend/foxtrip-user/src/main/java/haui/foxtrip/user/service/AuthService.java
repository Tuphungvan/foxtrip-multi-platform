package haui.foxtrip.user.service;

import haui.foxtrip.user.service.dto.request.*;
import haui.foxtrip.user.service.dto.response.AuthResponse;

public interface AuthService {

    String register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    AuthResponse refreshToken(RefreshAndLogoutTokenRequest request);

    void logout(RefreshAndLogoutTokenRequest request);
}