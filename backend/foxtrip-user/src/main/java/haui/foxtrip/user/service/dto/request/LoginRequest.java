package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotNull
    @Email
    @Size(min = 10, max = 50)
    private String email;

    @NotNull
    private String password;
}
