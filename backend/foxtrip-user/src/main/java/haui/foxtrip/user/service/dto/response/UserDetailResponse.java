package haui.foxtrip.user.service.dto.response;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.user.domain.enums.Role;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class UserDetailResponse {
    private UUID id;
    private String username;
    private String email;
    private String phoneNumber;
    private Province province;
    private Role role;
    private Boolean isVerified;
    private String avatarUrl;
    private Instant deletedAt;
}
