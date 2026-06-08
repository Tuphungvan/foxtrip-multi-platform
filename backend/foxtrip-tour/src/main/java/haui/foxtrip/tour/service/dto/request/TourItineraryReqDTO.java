package haui.foxtrip.tour.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class TourItineraryReqDTO {

    @Valid
    @NotNull
    private List<ItineraryItemReq> items;

    @Data
    public static class ItineraryItemReq {
        @NotNull
        private Integer dayNumber;

        @NotNull
        private Integer position;

        private UUID locationId;

        @NotBlank
        private String activity;
    }
}
