package haui.foxtrip.review.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.review.service.RevenueReportService;
import haui.foxtrip.review.service.dto.RevenueReportResDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final RevenueReportService revenueReportService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RevenueReportResDTO>> getRevenueReport(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam("year") @Min(2000) @Max(2100) Integer year) {
        RevenueReportResDTO report = revenueReportService.getRevenueReport(month, year);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công", report));
    }

    @GetMapping("/revenue/latest")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RevenueReportResDTO>>> getLatest12MonthsRevenueReport() {
        List<RevenueReportResDTO> reports = revenueReportService.getLatest12MonthsRevenueReport();
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo 12 tháng gần nhất thành công", reports));
    }
}
