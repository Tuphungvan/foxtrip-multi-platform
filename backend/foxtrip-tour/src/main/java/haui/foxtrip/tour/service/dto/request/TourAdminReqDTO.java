package haui.foxtrip.tour.service.dto.request;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.tour.domain.enums.TourCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class TourAdminReqDTO {

    @NotBlank(message = "Tên tour không được trống")
    private String name;

    @NotBlank(message = "Mô tả không được trống")
    private String description;

    @NotNull(message = "Tỉnh/Thành phố không được trống")
    private Province province;

    @NotNull(message = "Danh mục không được trống")
    private TourCategory category;

    @NotBlank(message = "Link short không được trống")
    private String shortId;

    @NotBlank(message = "Ảnh bìa Tour không được để trống")
    private String thumbnailUrl;

    @NotNull(message = "Giá không được trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;

    @NotNull(message = "Giảm giá không được trống")
    @Min(value = 0, message = "Giảm giá không được âm")
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull(message = "Số chỗ không được trống")
    @Min(value = 1, message = "Số chỗ phải lớn hơn 0")
    private Integer slots;

    @NotNull(message = "Ngày bắt đầu không được trống")
    @Future(message = "Ngày bắt đầu phải ở trong tương lai")
    private Instant startDate;

    @NotNull(message = "Ngày kết thúc không được trống")
    @Future(message = "Ngày kết thúc phải ở trong tương lai")
    private Instant endDate;

}
