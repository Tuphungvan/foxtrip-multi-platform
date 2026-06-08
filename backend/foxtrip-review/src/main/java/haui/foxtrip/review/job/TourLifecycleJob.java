package haui.foxtrip.review.job;

import haui.foxtrip.order.service.OrderService;
import haui.foxtrip.order.service.dto.RevenueSplitDTO;
import haui.foxtrip.review.service.RevenueReportService;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourLifecycleJob {

    private final TourRepository tourRepository;
    private final OrderService orderService;
    private final RevenueReportService revenueReportService;
    private final CacheManager cacheManager;

    /**
     * Chạy định kỳ mỗi giờ để cập nhật trạng thái Tour và Doanh thu
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void processTourLifecycle() {
        log.info("Starting Tour Lifecycle and Revenue Sync job");
        Instant now = Instant.now();

        boolean isUpdated = false;

        // 1. Chuyển ACTIVE -> ONGOING khi đến ngày khởi hành
        if (processStatusTransition(TourStatus.ACTIVE, TourStatus.ONGOING, now, true)) {
            isUpdated = true;
        }

        // 2. Chuyển ONGOING -> COMPLETED khi kết thúc tour
        processHistoryAndRevenue(now);

        if (isUpdated) {
            log.info("Tours were updated, clearing map_discovery cache");
            if (cacheManager.getCache("map_discovery") != null) {
                cacheManager.getCache("map_discovery").clear();
            }
        }

        log.info("Finished Tour Lifecycle job");
    }

    private boolean processStatusTransition(TourStatus from, TourStatus to, Instant now, boolean isStart) {
        List<Tour> tours;
        if (isStart) {
            tours = tourRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getStatus() == from && t.getStartDate().isBefore(now))
                .collect(Collectors.toList());
        } else {
            tours = tourRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getStatus() == from && t.getEndDate().isBefore(now))
                .collect(Collectors.toList());
        }

        if (tours.isEmpty()) return false;

        for (Tour tour : tours) {
            tour.setStatus(to);
            tourRepository.save(tour);
            log.info("Tour {} transitioned from {} to {}", tour.getName(), from, to);
        }
        return true;
    }

    private boolean processHistoryAndRevenue(Instant now) {
        List<Tour> finishingTours = tourRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getStatus() == TourStatus.ONGOING && t.getEndDate().isBefore(now))
                .collect(Collectors.toList());

        if (finishingTours.isEmpty()) return false;

        for (Tour tour : finishingTours) {
            tour.setStatus(TourStatus.COMPLETED);
            tourRepository.save(tour);
            log.info("Tour {} is now COMPLETED. Triggering order completion...", tour.getName());

            orderService.processTourCompletion(tour.getId(), tour.getName());

            RevenueSplitDTO split = orderService.getRevenueSplitByTour(tour.getId());
            if (split.getOrderCount() > 0) {
                ZonedDateTime zdt = tour.getEndDate().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
                revenueReportService.incrementRevenue(zdt.getMonthValue(), zdt.getYear(), split.getTourAmount(), split.getAddonAmount(), split.getOrderCount());
            }
        }
        return true;
    }
}
