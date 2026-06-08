package haui.foxtrip.tour.service.dto.response;

import java.util.UUID;
import lombok.Data;

@Data
public class GuideResDTO {
    private UUID id;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
}
