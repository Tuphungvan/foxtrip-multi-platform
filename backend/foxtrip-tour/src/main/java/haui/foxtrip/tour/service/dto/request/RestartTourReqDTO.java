package haui.foxtrip.tour.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class RestartTourReqDTO {

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;

    @NotNull
    @Min(0)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private BigDecimal discount;

    @NotNull
    @Min(1)
    private Integer slots;

    private UUID guideId;

    @NotBlank
    private String thumbnailUrl;

    @NotBlank
    private String shortId; // Video URL

    @NotBlank
    private String description;

    @Valid
    @NotNull
    private List<TourItineraryReqDTO.ItineraryItemReq> itineraries;

    @Valid
    private List<TourAddonReqDTO.AddonItemReq> addons;
}
