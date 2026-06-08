package vn.androidhaui.foxtrip.models.dto.response;

public class AuthResponse {
    public final String accessToken;
    public final String refreshToken;
    public final Long expiresIn;
    public final String role;

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.role = role;
    }
}
