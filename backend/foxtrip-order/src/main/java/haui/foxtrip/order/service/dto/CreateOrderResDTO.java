package haui.foxtrip.order.service.dto;

import haui.foxtrip.order.domain.enums.OrderStatus;
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
public class CreateOrderResDTO {
    private UUID orderId;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant expiresAt;
}
