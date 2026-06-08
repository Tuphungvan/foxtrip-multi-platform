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
public class MyOrderListItemDTO {
    private UUID orderId;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant paidAt;
    private TourSnapshotDTO tour;
    private Integer quantity;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourSnapshotDTO {
        private UUID tourId;
        private String tourNameAtTime;
        private Instant startDateAtTime;
        private Instant endDateAtTime;
        private String thumbnailAtTime;
    }
}
