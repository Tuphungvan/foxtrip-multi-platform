package haui.foxtrip.review.service;

import haui.foxtrip.location.domain.Location;
import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.order.repository.OrderRepository;
import haui.foxtrip.review.domain.RevenueReport;
import haui.foxtrip.review.repository.RevenueReportRepository;
import haui.foxtrip.review.service.RevenueReportService;
import haui.foxtrip.review.service.dto.RevenueReportResDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueReportServiceImpl implements RevenueReportService {

    private final RevenueReportRepository revenueReportRepository;
    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResDTO getRevenueReport(Integer month, Integer year) {
        RevenueReport current = revenueReportRepository.findByYearAndMonth(year, month).orElse(null);
        
        // Lấy dữ liệu tháng trước để tính tăng trưởng
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;
        RevenueReport prev = revenueReportRepository.findByYearAndMonth(prevYear, prevMonth).orElse(null);

        RevenueReportResDTO.RevenueReportResDTOBuilder builder = RevenueReportResDTO.builder()
                .month(month)
                .year(year);

        if (current == null) {
            builder.revenueTour(BigDecimal.ZERO)
                   .revenueAddon(BigDecimal.ZERO)
                   .revenueAds(BigDecimal.ZERO)
                   .totalRevenue(BigDecimal.ZERO)
                   .totalOrders(0);
        } else {
            builder.revenueTour(current.getRevenueTour())
                   .revenueAddon(current.getRevenueAddon())
                   .revenueAds(current.getRevenueAds())
                   .totalRevenue(current.getTotalRevenue())
                   .totalOrders(current.getTotalOrders());
        }

        // Tính toán tăng trưởng (%)
        if (prev != null && current != null) {
            builder.tourGrowth(calculateGrowth(current.getRevenueTour(), prev.getRevenueTour()));
            builder.addonGrowth(calculateGrowth(current.getRevenueAddon(), prev.getRevenueAddon()));
            builder.adsGrowth(calculateGrowth(current.getRevenueAds(), prev.getRevenueAds()));
            builder.totalGrowth(calculateGrowth(current.getTotalRevenue(), prev.getTotalRevenue()));
        } else if (current != null) {
            // Có tháng này nhưng không tìm thấy tháng trước
            builder.tourGrowth(BigDecimal.ZERO).addonGrowth(BigDecimal.ZERO).adsGrowth(BigDecimal.ZERO).totalGrowth(BigDecimal.ZERO);
        }

        return builder.build();
    }

    private BigDecimal calculateGrowth(BigDecimal current, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        if (current == null) return new BigDecimal("-100");
        
        return current.subtract(prev)
                .divide(prev, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<RevenueReportResDTO> getLatest12MonthsRevenueReport() {
        return revenueReportRepository.findTop12ByOrderByYearDescMonthDesc()
                .stream()
                .sorted(java.util.Comparator.comparing(RevenueReport::getYear).thenComparing(RevenueReport::getMonth))
                .map(report -> RevenueReportResDTO.builder()
                        .month(report.getMonth())
                        .year(report.getYear())
                        .revenueTour(report.getRevenueTour())
                        .revenueAddon(report.getRevenueAddon())
                        .revenueAds(report.getRevenueAds())
                        .totalRevenue(report.getTotalRevenue())
                        .totalOrders(report.getTotalOrders())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void syncRevenueReport(int month, int year) {
        log.info("Syncing revenue report for month: {} year: {}", month, year);
        
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, vnZone);
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1);
        
        Instant since = startOfMonth.toInstant();
        Instant until = endOfMonth.toInstant();
        
        BigDecimal tourRevenue = orderRepository.calculateTourRevenueInPeriod(since, until);
        BigDecimal addonRevenue = orderRepository.calculateAddonRevenueInPeriod(since, until);
        long totalOrders = orderRepository.countCompletedOrdersInPeriod(since, until);
        
        // Calculate Ads Revenue from Locations
        BigDecimal adsRevenue = BigDecimal.ZERO;
        List<Location> featuredLocations = locationRepository.findFeaturedByStartMonthAndYear(month, year);
        for (Location l : featuredLocations) {
            BigDecimal pricePerDay = switch (l.getPriority()) {
                case 1 -> new BigDecimal("50000");
                case 2 -> new BigDecimal("100000");
                case 3 -> new BigDecimal("200000");
                default -> BigDecimal.ZERO;
            };
            long days = ChronoUnit.DAYS.between(l.getFeaturedStartAt(), l.getFeaturedEndAt()) + 1;
            if (days > 0) {
                adsRevenue = adsRevenue.add(pricePerDay.multiply(BigDecimal.valueOf(days)));
            }
        }
        
        if (tourRevenue == null) tourRevenue = BigDecimal.ZERO;
        if (addonRevenue == null) addonRevenue = BigDecimal.ZERO;
        BigDecimal totalRevenue = tourRevenue.add(addonRevenue).add(adsRevenue);

        RevenueReport report = revenueReportRepository.findByYearAndMonth(year, month)
            .orElseGet(() -> {
                RevenueReport newReport = new RevenueReport();
                newReport.setMonth(month);
                newReport.setYear(year);
                return newReport;
            });

        report.setRevenueTour(tourRevenue);
        report.setRevenueAddon(addonRevenue);
        report.setRevenueAds(adsRevenue);
        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders((int) totalOrders);

        revenueReportRepository.save(report);
        log.info("Revenue report synced for {}/{}: Total Revenue = {}, Orders = {}", month, year, totalRevenue, totalOrders);
    }

    @Override
    @Transactional
    public void incrementRevenue(int month, int year, BigDecimal tourAmount, BigDecimal addonAmount, int orderCount) {
        log.info("Incrementing revenue for {}/{}: Tour = {}, Addon = {}, Orders = {}", 
                month, year, tourAmount, addonAmount, orderCount);

        RevenueReport report = revenueReportRepository.findByYearAndMonth(year, month)
            .orElseGet(() -> {
                RevenueReport newReport = new RevenueReport();
                newReport.setMonth(month);
                newReport.setYear(year);
                return newReport;
            });

        report.setRevenueTour(report.getRevenueTour().add(tourAmount));
        report.setRevenueAddon(report.getRevenueAddon().add(addonAmount));
        report.setTotalRevenue(report.getTotalRevenue().add(tourAmount).add(addonAmount));
        report.setTotalOrders(report.getTotalOrders() + orderCount);

        revenueReportRepository.save(report);
        log.info("Revenue incremented successfully. New total: {}", report.getTotalRevenue());
    }

    @Override
    @Transactional
    public void incrementAdsRevenue(int month, int year, BigDecimal amount) {
        log.info("Incrementing ads revenue for {}/{}: Amount = {}", month, year, amount);

        RevenueReport report = revenueReportRepository.findByYearAndMonth(year, month)
            .orElseGet(() -> {
                RevenueReport newReport = new RevenueReport();
                newReport.setMonth(month);
                newReport.setYear(year);
                return newReport;
            });

        report.setRevenueAds(report.getRevenueAds().add(amount));
        report.setTotalRevenue(report.getTotalRevenue().add(amount));

        revenueReportRepository.save(report);
        log.info("Ads revenue incremented successfully. New total: {}", report.getTotalRevenue());
    }
}
