package haui.foxtrip.order.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSplitDTO {
    private BigDecimal tourAmount;
    private BigDecimal addonAmount;
    private int orderCount;
}
