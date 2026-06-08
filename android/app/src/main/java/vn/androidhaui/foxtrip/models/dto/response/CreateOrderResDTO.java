package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CreateOrderResDTO implements Serializable {

    @SerializedName("orderId")
    public String orderId;

    @SerializedName("orderCode")
    public String orderCode;

    @SerializedName("status")
    public String status;

    @SerializedName("totalAmount")
    public Double totalAmount;

    @SerializedName("expiresAt")
    public String expiresAt;

    public CreateOrderResDTO() {}
}
