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
public class AdminOrderListItemDTO {
    private UUID orderId;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant paidAt;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String tourNameAtTime;
    private Instant startDateAtTime;
    private Integer quantity;
}
