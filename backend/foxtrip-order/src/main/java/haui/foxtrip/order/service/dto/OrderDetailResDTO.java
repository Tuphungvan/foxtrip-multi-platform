package haui.foxtrip.order.service.dto;

import haui.foxtrip.order.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResDTO {
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
    private TourSnapshotDTO tour;
    private Integer quantity;
    private List<AddonSnapshotDTO> addons;
    private String qrPayload;
    private UUID refundId;
    
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonSnapshotDTO {
        private UUID tourAddonId;
        private String nameAtTime;
        private BigDecimal unitPriceAtTime;
        private Integer quantity;
    }
}
