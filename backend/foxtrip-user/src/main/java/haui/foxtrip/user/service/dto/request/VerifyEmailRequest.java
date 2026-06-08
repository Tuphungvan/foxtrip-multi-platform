package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotNull
    @Email
    @Size(min = 10, max = 50)
    private String email;

    @NotNull
    @Pattern(regexp = "^[0-9]{6}$")
    private String otp;
}
