package haui.foxtrip.review.service;

import haui.foxtrip.review.service.dto.RevenueReportResDTO;
import java.math.BigDecimal;
import java.util.List;

import java.util.List;

public interface RevenueReportService {

    RevenueReportResDTO getRevenueReport(Integer month, Integer year);

    void syncRevenueReport(int month, int year);

    void incrementRevenue(int month, int year, BigDecimal tourAmount, BigDecimal addonAmount, int orderCount);

    void incrementAdsRevenue(int month, int year, BigDecimal amount);

    List<RevenueReportResDTO> getLatest12MonthsRevenueReport();
}
