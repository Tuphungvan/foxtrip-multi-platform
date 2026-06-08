package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PaymentInitResDTO implements Serializable {

    @SerializedName("paymentUrl")
    public String paymentUrl;

    @SerializedName("orderId")
    public String orderId;

    @SerializedName("orderCode")
    public String orderCode;

    @SerializedName("expiresAt")
    public String expiresAt;

    public PaymentInitResDTO() {}
}
