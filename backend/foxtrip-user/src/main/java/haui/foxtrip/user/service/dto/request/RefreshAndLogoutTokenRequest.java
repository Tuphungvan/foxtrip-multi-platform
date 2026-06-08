package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshAndLogoutTokenRequest {
    @NotNull
    private String refreshToken;
}
