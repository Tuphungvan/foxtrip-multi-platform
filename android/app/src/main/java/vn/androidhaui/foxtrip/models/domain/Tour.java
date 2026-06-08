package vn.androidhaui.foxtrip.models.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Tour {
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("province")
    private String province;

    @SerializedName("region")
    private String region;

    @SerializedName("category")
    private String category;

    @SerializedName("image")
    private List<String> image;

    @SerializedName("videoId")
    private String videoId;

    @SerializedName("startDate")
    private Date startDate;

    @SerializedName("endDate")
    private Date endDate;

    @SerializedName("itinerary")
    private String itinerary;

    @SerializedName("price")
    private Double price;

    @SerializedName("slug")
    private String slug;

    @SerializedName("slots")
    private Integer slots;

    @SerializedName("availableSlots")
    private Integer availableSlots;

    @SerializedName("isBookable")
    private Boolean isBookable;

    @SerializedName("discount")
    private Integer discount;

    @SerializedName("shortUrl")
    private String shortUrl;

    // Getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getImage() { return image; }
    public void setImage(List<String> image) {
        this.image = image;
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getItinerary() { return itinerary; }
    public void setItinerary(String itinerary) { this.itinerary = itinerary; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Integer getSlots() { return slots; }
    public void setSlots(Integer slots) { this.slots = slots; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public Boolean getIsBookable() { return isBookable; }
    public void setIsBookable(Boolean isBookable) { this.isBookable = isBookable; }

    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tour tour = (Tour) obj;
        return slug != null && slug.equals(tour.slug);
    }

    @Override
    public int hashCode() {
        return slug != null ? slug.hashCode() : 0;
    }


}
