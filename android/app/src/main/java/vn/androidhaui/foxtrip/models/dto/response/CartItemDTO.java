package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CartItemDTO implements Serializable {

    @SerializedName("tourId")
    public String tourId;

    @SerializedName("quantity")
    public Integer quantity;

    @SerializedName("tour")
    public TourInfo tour;

    public int getSafeQuantity() {
        return quantity != null ? quantity : 0;
    }

    public static class TourInfo implements Serializable {

        @SerializedName("slug")
        public String slug;

        @SerializedName("name")
        public String name;

        @SerializedName("thumbnailUrl")
        public String thumbnailUrl;

        @SerializedName("startDate")
        public String startDate;

        @SerializedName("endDate")
        public String endDate;

        @SerializedName("price")
        public Double price;

        @SerializedName("discount")
        public Double discount;

        @SerializedName("availableSlots")
        public Integer availableSlots;

        /** Danh sách addon khả dụng — field backend: "addons" */
        @SerializedName("addons")
        public List<TourAddonItem> addons;

        public double getFinalPrice() {
            double p = price != null ? price : 0;
            double d = discount != null ? discount : 0;
            return Math.max(0, p * (1 - d / 100.0));
        }

        public List<TourAddonItem> getSafeAddons() {
            return addons != null ? addons : new ArrayList<>();
        }
    }
}
