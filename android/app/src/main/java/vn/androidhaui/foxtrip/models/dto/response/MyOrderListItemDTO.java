package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MyOrderListItemDTO implements Serializable {

    @SerializedName("orderId")
    public String orderId;

    @SerializedName("orderCode")
    public String orderCode;

    @SerializedName("status")
    public String status;

    @SerializedName("totalAmount")
    public Double totalAmount;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("expiresAt")
    public String expiresAt;

    @SerializedName("paidAt")
    public String paidAt;

    @SerializedName("tour")
    public TourSnapshotDTO tour;

    @SerializedName("quantity")
    public Integer quantity;

    public static class TourSnapshotDTO implements Serializable {
        @SerializedName("tourId")
        public String tourId;

        @SerializedName("tourNameAtTime")
        public String tourNameAtTime;

        @SerializedName("startDateAtTime")
        public String startDateAtTime;

        @SerializedName("endDateAtTime")
        public String endDateAtTime;

        @SerializedName("thumbnailAtTime")
        public String thumbnailAtTime;
    }
}
