package haui.foxtrip.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import haui.foxtrip.tour.service.dto.response.TourAddonResDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
public class CartResDTO {

    private List<CartItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartItemDTO {

        private UUID tourId;
        private Integer quantity;
        private TourInfoDTO tour;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TourInfoDTO {

        private String slug;
        private String name;
        private String thumbnailUrl;
        private Instant startDate;
        private Instant endDate;
        private BigDecimal price;
        private BigDecimal discount;
        private Integer availableSlots;
        private List<TourAddonResDTO> addons;
    }
}
