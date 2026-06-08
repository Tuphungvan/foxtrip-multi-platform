package haui.foxtrip.tour.service.dto.request;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.tour.domain.enums.TourCategory;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class UpdateTourReqDTO {

    private String name;

    private String description;

    private Province province;

    private TourCategory category;

    @Min(value = 1, message = "Số chỗ phải lớn hơn 0")
    private Integer slots;

    private Instant startDate;

    private Instant endDate;

    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;

    @Min(value = 0, message = "Giảm giá không được âm")
    private BigDecimal discount;

    private String thumbnailUrl;

    private String shortId;
}
