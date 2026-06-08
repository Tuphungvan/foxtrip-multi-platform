package vn.androidhaui.foxtrip.models.dto.request;

import com.google.gson.annotations.SerializedName;

public class UpsertCartItemRequest {

    @SerializedName("tourId")
    private String tourId;

    @SerializedName("quantity")
    private int quantity;

    public UpsertCartItemRequest(String tourId, int quantity) {
        this.tourId = tourId;
        this.quantity = quantity;
    }
}
