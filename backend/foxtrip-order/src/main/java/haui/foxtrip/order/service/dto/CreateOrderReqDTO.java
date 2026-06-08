package haui.foxtrip.order.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderReqDTO {
    
    @NotNull(message = "fromCart không được để trống")
    private Boolean fromCart;
    
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có đúng 10 chữ số")
    private String customerPhone;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String customerEmail;
    
    @NotNull(message = "Tour ID không được để trống")
    private UUID tourId;
    
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer quantity;
    
    @Valid
    private AddonsWrapperDTO addons;
}
