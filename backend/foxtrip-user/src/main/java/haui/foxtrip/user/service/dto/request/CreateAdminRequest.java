package haui.foxtrip.user.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAdminRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(min = 10, max = 50, message = "Email phải từ 10 đến 50 ký tự")
    private String email;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 8, max = 50, message = "Username phải từ 8 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải là 10 chữ số")
    private String phoneNumber;
}
