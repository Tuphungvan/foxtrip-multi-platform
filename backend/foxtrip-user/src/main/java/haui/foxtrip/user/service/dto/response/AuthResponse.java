package haui.foxtrip.user.service.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String role;
}
