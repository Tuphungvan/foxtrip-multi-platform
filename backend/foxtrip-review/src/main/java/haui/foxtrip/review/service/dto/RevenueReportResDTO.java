package haui.foxtrip.review.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueReportResDTO {

    private Integer month;
    private Integer year;
    private BigDecimal revenueTour;
    private BigDecimal revenueAddon;
    private BigDecimal revenueAds;
    private BigDecimal totalRevenue;
    private Integer totalOrders;

    private BigDecimal tourGrowth;
    private BigDecimal addonGrowth;
    private BigDecimal adsGrowth;
    private BigDecimal totalGrowth;
}
