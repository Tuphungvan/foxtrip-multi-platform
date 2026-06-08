package haui.foxtrip.tour.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class TourAddonReqDTO {

    @Valid
    @NotNull
    private List<AddonItemReq> addons;

    @Data
    public static class AddonItemReq {
        private UUID id; // optional; null = tạo mới

        @NotBlank
        private String name;

        @NotBlank(message = "Mô tả add-on không được trống")
        private String description;

        @NotNull
        @Min(0)
        private BigDecimal price;

        @NotNull
        private Boolean isActive;

        private UUID locationId; // optional
    }
}
