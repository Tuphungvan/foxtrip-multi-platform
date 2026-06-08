package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckoutPreviewResponse {

    @SerializedName("tour")
    public TourInfo tour;

    @SerializedName("quantity")
    public Integer quantity;

    @SerializedName("price")
    public Double price;

    @SerializedName("discount")
    public Double discount;

    @SerializedName("addons")
    public List<AddonInfo> addons;

    @SerializedName("totalAmount")
    public Double totalAmount;

    public static class TourInfo {
        @SerializedName("id")
        public String id;

        @SerializedName("slug")
        public String slug;

        @SerializedName("name")
        public String name;

        @SerializedName("thumbnailUrl")
        public String thumbnailUrl;
    }

    public static class AddonInfo {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("unitPrice")
        public Double unitPrice;

        @SerializedName("quantity")
        public Integer quantity;

        @SerializedName("total")
        public Double total;
    }
}
