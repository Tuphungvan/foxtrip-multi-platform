package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LocationResponse implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("province")
    public String province;

    @SerializedName("address")
    public String address;

    @SerializedName("type")
    public String type;

    @SerializedName("contactPhone")
    public String contactPhone;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("lat")
    public Double lat;

    @SerializedName("lng")
    public Double lng;

    @SerializedName("priority")
    public Integer priority;

    @SerializedName("featuredStartAt")
    public String featuredStartAt;

    @SerializedName("featuredEndAt")
    public String featuredEndAt;
}
