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
public class ReviewResDTO {

    private UUID reviewId;
    private UUID tourId;
    private Integer rating;
    private String content;
    private Instant createdAt;
    private Instant editedAt;
}
