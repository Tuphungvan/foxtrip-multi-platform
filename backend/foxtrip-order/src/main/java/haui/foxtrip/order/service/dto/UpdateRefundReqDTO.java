package haui.foxtrip.order.service.dto;

import haui.foxtrip.order.domain.enums.RefundStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRefundReqDTO {
    
    @NotNull(message = "Trạng thái không được để trống")
    private RefundStatus status;
    
    private String note;
}
