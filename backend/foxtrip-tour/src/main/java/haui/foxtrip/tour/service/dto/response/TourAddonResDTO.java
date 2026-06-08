package haui.foxtrip.tour.service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class TourAddonResDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isActive;
}
