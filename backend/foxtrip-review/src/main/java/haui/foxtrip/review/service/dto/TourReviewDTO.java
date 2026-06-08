package haui.foxtrip.review.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourReviewDTO {

    private UUID reviewId;
    private Integer rating;
    private String content;
    private Instant createdAt;
    private Instant editedAt;
    private UserInfoDTO user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfoDTO {

        private String username;
        private String avatarUrl;
    }
}
