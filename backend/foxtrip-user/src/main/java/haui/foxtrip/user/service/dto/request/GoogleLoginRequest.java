package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotNull
    private String idToken;
}
