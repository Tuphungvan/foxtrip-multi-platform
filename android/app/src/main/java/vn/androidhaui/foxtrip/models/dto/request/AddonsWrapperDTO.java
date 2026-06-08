package vn.androidhaui.foxtrip.models.dto.request;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class AddonsWrapperDTO implements Serializable {

    @SerializedName("items")
    public List<AddonItemDTO> items;

    public AddonsWrapperDTO() {}

    public AddonsWrapperDTO(List<AddonItemDTO> items) {
        this.items = items;
    }
}
