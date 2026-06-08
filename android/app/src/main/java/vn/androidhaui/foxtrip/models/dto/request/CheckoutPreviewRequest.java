package vn.androidhaui.foxtrip.models.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckoutPreviewRequest {

    @SerializedName("fromCart")
    private boolean fromCart;

    @SerializedName("tourId")
    private String tourId;

    @SerializedName("addons")
    private AddonsWrapper addons;

    public CheckoutPreviewRequest(String tourId, boolean fromCart, List<AddonItem> selectedAddons) {
        this.tourId = tourId;
        this.fromCart = fromCart;
        if (selectedAddons != null && !selectedAddons.isEmpty()) {
            this.addons = new AddonsWrapper(selectedAddons);
        }
    }

    public static class AddonsWrapper {
        @SerializedName("items")
        public List<AddonItem> items;

        public AddonsWrapper(List<AddonItem> items) {
            this.items = items;
        }
    }

    public static class AddonItem {
        @SerializedName("tourAddonId")
        public String tourAddonId;

        @SerializedName("quantity")
        public int quantity;

        public AddonItem(String tourAddonId, int quantity) {
            this.tourAddonId = tourAddonId;
            this.quantity = quantity;
        }
    }
}
