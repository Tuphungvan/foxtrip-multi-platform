package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MarkerResponse implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public String type; // "TOUR" or "AD"

    @SerializedName("priority")
    public Integer priority;

    @SerializedName("address")
    public String address;

    @SerializedName("lat")
    public Double lat;

    @SerializedName("lng")
    public Double lng;

    @SerializedName("title")
    public String title;

    @SerializedName("thumbnailUrl")
    public String thumbnailUrl;

    // Attributes for TOUR
    @SerializedName("price")
    public Double price;

    @SerializedName("discount")
    public Double discount;

    @SerializedName("averageRating")
    public Double averageRating;

    @SerializedName("slug")
    public String slug;
}
