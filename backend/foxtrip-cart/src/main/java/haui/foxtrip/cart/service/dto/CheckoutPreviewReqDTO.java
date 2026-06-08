package haui.foxtrip.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CheckoutPreviewReqDTO {

    @NotNull(message = "fromCart không được để trống")
    private Boolean fromCart;

    @NotNull(message = "Tour ID không được để trống")
    private UUID tourId;

    private Integer quantity; // Required khi fromCart=false

    @Valid
    private AddonsWrapper addons;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddonsWrapper {

        private List<AddonItemDTO> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddonItemDTO {

        @NotNull(message = "Tour addon ID không được để trống")
        private UUID tourAddonId;

        @NotNull(message = "Số lượng addon không được để trống")
        @Min(value = 1, message = "Số lượng addon phải >= 1")
        private Integer quantity;
    }
}
