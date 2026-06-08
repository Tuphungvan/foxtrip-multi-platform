package haui.foxtrip.review.service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ComprehensiveReportDTO {
    private List<TourOccupancyDTO> occupancyReport;
    private RevenueReportResDTO revenueReport;
    private long adLocationCount;
}
