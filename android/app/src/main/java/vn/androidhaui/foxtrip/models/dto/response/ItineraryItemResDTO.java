package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ItineraryItemResDTO implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("dayNumber")
    public Integer dayNumber;

    @SerializedName("position")
    public Integer position;

    @SerializedName("activity")
    public String activity;

    @SerializedName("locationId")
    public String locationId;

    @SerializedName("locationName")
    public String locationName;

    @SerializedName("locationImageUrl")
    public String locationImageUrl;

    @SerializedName("lat")
    public Double lat;

    @SerializedName("lng")
    public Double lng;
}
