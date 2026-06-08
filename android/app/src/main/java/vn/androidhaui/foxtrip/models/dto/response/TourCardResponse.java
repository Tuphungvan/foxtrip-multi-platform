package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class TourCardResponse implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("slug")
    public String slug;

    @SerializedName("name")
    public String name;

    @SerializedName("province")
    public String province;

    @SerializedName("thumbnailUrl")
    public String thumbnailUrl;

    @SerializedName("price")
    public Double price;

    @SerializedName("discount")
    public Double discount;

    @SerializedName("finalPrice")
    public Double finalPrice;

    @SerializedName("startDate")
    public Date startDate;

    @SerializedName("averageRating")
    public Double averageRating;
}
