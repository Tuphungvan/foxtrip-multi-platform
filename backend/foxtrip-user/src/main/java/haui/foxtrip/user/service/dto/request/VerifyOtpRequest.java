package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotNull
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải là 6 chữ số")
    private String otp;
}
