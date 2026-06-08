package vn.androidhaui.foxtrip.models.dto.request;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CreateOrderReqDTO implements Serializable {

    @SerializedName("fromCart")
    public Boolean fromCart;

    @SerializedName("customerName")
    public String customerName;

    @SerializedName("customerPhone")
    public String customerPhone;

    @SerializedName("customerEmail")
    public String customerEmail;

    @SerializedName("tourId")
    public String tourId;

    @SerializedName("quantity")
    public Integer quantity;

    @SerializedName("addons")
    public AddonsWrapperDTO addons;

    public CreateOrderReqDTO() {}

    public CreateOrderReqDTO(Boolean fromCart, String customerName, String customerPhone, 
                             String customerEmail, String tourId, Integer quantity, 
                             AddonsWrapperDTO addons) {
        this.fromCart = fromCart;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.tourId = tourId;
        this.quantity = quantity;
        this.addons = addons;
    }
}
