package haui.foxtrip.location.service.dto.request;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.location.domain.enums.LocType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.Instant;

@Data
public class UpdateLocationRequest {

    private String name;
    private Province province;
    private String address;
    private String imageUrl;
    private LocType type;
    private Double lat;
    private Double lng;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải là 10 chữ số")
    private String contactPhone;
    @Max(3)
    private Integer priority;
    private Instant featuredStartAt;
    private Instant featuredEndAt;

    private String mapboxPlaceId;
}