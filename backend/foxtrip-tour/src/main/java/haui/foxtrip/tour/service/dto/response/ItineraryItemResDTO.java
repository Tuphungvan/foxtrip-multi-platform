package haui.foxtrip.tour.service.dto.response;

import java.util.UUID;
import lombok.Data;

@Data
public class ItineraryItemResDTO {
    private UUID id;
    private Integer dayNumber;
    private Integer position;
    private String activity;
    
    private UUID locationId;
    private String locationName;
    private String locationImageUrl;
    private Double lat;
    private Double lng;
}
