package haui.foxtrip.order.service.dto;

import haui.foxtrip.order.domain.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResDTO {
    private UUID refundId;
    private UUID orderId;
    private BigDecimal amount;
    private RefundStatus status;
    private String reason;
    private Instant processedAt;
}
