package vn.androidhaui.foxtrip.models.dto.response;

import java.io.Serializable;
import java.util.UUID;

public class TourListResDTO implements Serializable {
    public UUID id;
    public String slug;
    public String shortId;
    public String name;
    public String province; // Enum mapping as string
    public String category; // Enum mapping as string
    public String thumbnailUrl;
    public Double price;
    public Double discount;
    public Double finalPrice;
    public String startDate;
    public String endDate;
    public Integer availableSlots;
    public Double averageRating;
    public Integer reviewCount;
    public String status;
}
