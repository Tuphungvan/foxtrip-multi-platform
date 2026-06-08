package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotNull
    @Email
    @Size(min = 10, max = 50)
    private String email;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
    private String password;

    @NotNull
    @Size(min = 8, max = 50)
    private String username;

    @NotNull
    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;

    private Boolean sendOtp = true;
}
