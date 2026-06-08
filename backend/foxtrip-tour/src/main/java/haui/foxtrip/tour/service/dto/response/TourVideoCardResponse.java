package haui.foxtrip.tour.service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourVideoCardResponse {
    private UUID id;
    private String shortId; // YouTube Short ID
    private String title;
    private String slug;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal averageRating;
}
