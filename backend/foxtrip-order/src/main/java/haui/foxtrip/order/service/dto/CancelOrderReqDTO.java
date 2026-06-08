package haui.foxtrip.order.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelOrderReqDTO {
    
    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
}
