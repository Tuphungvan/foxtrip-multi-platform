package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class CartResponseDTO {

    @SerializedName("items")
    public List<CartItemDTO> items;

    public List<CartItemDTO> safeItems() {
        return items != null ? items : new ArrayList<>();
    }
}
