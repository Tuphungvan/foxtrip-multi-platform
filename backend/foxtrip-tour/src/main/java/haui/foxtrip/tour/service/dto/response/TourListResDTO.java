package haui.foxtrip.tour.service.dto.response;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.tour.domain.enums.TourCategory;
import haui.foxtrip.tour.domain.enums.TourSetupStep;
import haui.foxtrip.tour.domain.enums.TourStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class TourListResDTO {
    private UUID id;
    private String slug;
    private String shortId;
    private String name;
    private Province province;
    private TourCategory category;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private Instant startDate;
    private Instant endDate;
    private Integer availableSlots;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private TourStatus status;
    private TourSetupStep setupStep;
}
