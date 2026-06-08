package haui.foxtrip.location.service.dto.request;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.location.domain.enums.LocType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.Instant;

@Data
public class CreateLocationRequest {

    @NotBlank(message = "Tên địa điểm không được để trống")
    private String name;

    @NotNull(message = "Tỉnh/Thành phố không được để trống")
    private Province province;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Ảnh đại diện không được để trống")
    private String imageUrl;

    @NotNull(message = "Loại địa điểm không được để trống")
    private LocType type = LocType.OTHER;

    @NotNull(message = "Vĩ độ không được để trống")
    private Double lat;

    @NotNull(message = "Kinh độ không được để trống")
    private Double lng;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải là 10 chữ số")
    private String contactPhone;

    @Max(3)
    private Integer priority;
    private Instant featuredStartAt;
    private Instant featuredEndAt;

    private String mapboxPlaceId;
}
