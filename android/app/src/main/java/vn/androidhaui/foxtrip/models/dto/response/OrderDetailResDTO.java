package vn.androidhaui.foxtrip.models.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO chi tiết đơn hàng — map với OrderDetailResDTO từ Backend.
 */
public class OrderDetailResDTO {
    public String orderId;
    public String orderCode;
    public String status;
    public BigDecimal totalAmount;
    public String createdAt;   // ISO-8601 string (Instant serialized by Jackson)
    public String expiresAt;
    public String paidAt;

    public String customerName;
    public String customerPhone;
    public String customerEmail;

    public TourSnapshotDTO tour;
    public Integer quantity;
    public List<AddonSnapshotDTO> addons;
    public String qrPayload;

    public static class TourSnapshotDTO {
        public String tourId;
        public String tourNameAtTime;
        public String startDateAtTime;   // ISO-8601 string
        public String endDateAtTime;
        public String thumbnailAtTime;
    }

    public static class AddonSnapshotDTO {
        public String tourAddonId;
        public String nameAtTime;
        public BigDecimal unitPriceAtTime;
        public Integer quantity;
    }
}
