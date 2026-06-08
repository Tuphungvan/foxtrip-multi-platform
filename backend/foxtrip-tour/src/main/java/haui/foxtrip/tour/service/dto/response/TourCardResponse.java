package haui.foxtrip.tour.service.dto.response;

import haui.foxtrip.common.enums.Province;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class TourCardResponse {
    private UUID id;
    private String slug;
    private String name;
    private Province province;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private Instant startDate;
    private BigDecimal averageRating;
}
