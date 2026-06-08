package haui.foxtrip.location.service.dto.response;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.location.domain.enums.LocType;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class LocationResponse {

    private UUID id;
    private String name;
    private Province province;
    private String address;
    private LocType type;
    private String contactPhone;
    private String imageUrl;
    private Double lat;
    private Double lng;
    private Integer priority;
    private Instant featuredStartAt;
    private Instant featuredEndAt;
    private Instant deletedAt;
    private String mapboxPlaceId;
}
