package haui.foxtrip.order.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRefundReqDTO {
    
    @NotNull(message = "Số tiền hoàn không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền hoàn phải lớn hơn 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Lý do hoàn tiền không được để trống")
    private String reason;
}
