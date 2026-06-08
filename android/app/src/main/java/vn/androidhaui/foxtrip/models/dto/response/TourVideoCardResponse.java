package vn.androidhaui.foxtrip.models.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class TourVideoCardResponse {
    private UUID id;
    private String shortId; // YouTube Full URL
    private String title;
    private String slug;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal averageRating;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getShortId() { return shortId; }
    public void setShortId(String shortId) { this.shortId = shortId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public double getFinalPrice() {
        if (price == null) return 0;
        double d = discount != null ? discount.doubleValue() : 0;
        return price.doubleValue() - d;
    }
}
