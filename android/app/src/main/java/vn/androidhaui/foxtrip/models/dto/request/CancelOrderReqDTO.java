package vn.androidhaui.foxtrip.models.dto.request;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CancelOrderReqDTO implements Serializable {

    @SerializedName("reason")
    public String reason;

    public CancelOrderReqDTO() {}

    public CancelOrderReqDTO(String reason) {
        this.reason = reason;
    }
}
