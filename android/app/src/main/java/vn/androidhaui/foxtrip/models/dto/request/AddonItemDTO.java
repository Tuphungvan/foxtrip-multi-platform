package vn.androidhaui.foxtrip.models.dto.request;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AddonItemDTO implements Serializable {

    @SerializedName("tourAddonId")
    public String tourAddonId;

    @SerializedName("quantity")
    public Integer quantity;

    public AddonItemDTO() {}

    public AddonItemDTO(String tourAddonId, Integer quantity) {
        this.tourAddonId = tourAddonId;
        this.quantity = quantity;
    }
}
