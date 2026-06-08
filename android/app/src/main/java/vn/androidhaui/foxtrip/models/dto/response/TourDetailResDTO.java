package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class TourDetailResDTO implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("slug")
    public String slug;

    @SerializedName("shortId")
    public String shortId;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("province")
    public String province;

    @SerializedName("category")
    public String category;

    @SerializedName("thumbnailUrl")
    public String thumbnailUrl;

    @SerializedName("price")
    public Double price;

    @SerializedName("discount")
    public Double discount;

    @SerializedName("finalPrice")
    public Double finalPrice;

    @SerializedName("slots")
    public Integer slots;

    @SerializedName("availableSlots")
    public Integer availableSlots;

    @SerializedName("startDate")
    public java.util.Date startDate;

    @SerializedName("endDate")
    public java.util.Date endDate;

    @SerializedName("averageRating")
    public Double averageRating;

    @SerializedName("reviewCount")
    public Integer reviewCount;

    @SerializedName("bookable")
    public Boolean bookable;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("setupStep")
    public String setupStep;

    @SerializedName("itineraries")
    public List<ItineraryItemResDTO> itineraries;

    // Optional addons and guide
    // @SerializedName("addons")
    // public List<TourAddonResDTO> addons;
    @SerializedName("guide")
    public GuideResDTO guide;
}
