package haui.foxtrip.review.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.review.service.AdminStatisticService;
import haui.foxtrip.review.service.RevenueReportService;
import haui.foxtrip.review.service.dto.ComprehensiveReportDTO;
import haui.foxtrip.review.service.dto.DailyBookingDTO;
import haui.foxtrip.review.service.dto.RevenueReportResDTO;
import haui.foxtrip.review.service.dto.TourOccupancyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticController {

    private final AdminStatisticService adminStatisticService;
    private final RevenueReportService revenueReportService;

    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TourOccupancyDTO>>> getTourOccupancyReport(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        List<TourOccupancyDTO> report = adminStatisticService.getTourOccupancyReport(month, year);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo lấp đầy tour thành công", report));
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DailyBookingDTO>>> getDailyBookingReport(
            @RequestParam(value = "date", required = false) String date) {
        List<DailyBookingDTO> report = adminStatisticService.getDailyBookingReport(date);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo đặt chỗ ngày thành công", report));
    }

    @GetMapping("/comprehensive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ComprehensiveReportDTO>> getComprehensiveReport(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam("year") Integer year) {
        
        List<TourOccupancyDTO> occupancy = adminStatisticService.getTourOccupancyReport(month, year);
        RevenueReportResDTO revenue = revenueReportService.getRevenueReport(month, year);
        long adCount = adminStatisticService.countFeaturedLocations(month, year);
        
        ComprehensiveReportDTO report = ComprehensiveReportDTO.builder()
                .occupancyReport(occupancy)
                .revenueReport(revenue)
                .adLocationCount(adCount)
                .build();
                
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo tổng hợp thành công", report));
    }
}
