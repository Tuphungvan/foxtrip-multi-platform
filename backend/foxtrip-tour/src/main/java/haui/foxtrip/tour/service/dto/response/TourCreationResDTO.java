package haui.foxtrip.tour.service.dto.response;

import haui.foxtrip.tour.domain.enums.TourSetupStep;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCreationResDTO {
    private UUID id;
    private TourSetupStep setupStep;
}
