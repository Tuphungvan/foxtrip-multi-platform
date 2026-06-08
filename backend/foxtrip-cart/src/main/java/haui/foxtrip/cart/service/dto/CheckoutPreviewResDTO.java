package haui.foxtrip.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class CheckoutPreviewResDTO {

    private TourInfoDTO tour;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private List<AddonInfoDTO> addons;
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TourInfoDTO {

        private UUID id;
        private String slug;
        private String name;
        private String thumbnailUrl;
        private Instant startDate;
        private Instant endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddonInfoDTO {

        private UUID id;
        private String name;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal total;
    }
}
