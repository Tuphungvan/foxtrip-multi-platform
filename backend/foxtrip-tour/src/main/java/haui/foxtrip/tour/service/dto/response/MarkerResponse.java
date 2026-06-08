package haui.foxtrip.tour.service.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkerResponse {
    private String id;
    private String type; // "TOUR" or "AD"
    private Integer priority;
    private String address;
    private Double lat;
    private Double lng;
    private String title;
    private String thumbnailUrl;
    // Attributes for TOUR
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal averageRating;
    private String slug;
}
