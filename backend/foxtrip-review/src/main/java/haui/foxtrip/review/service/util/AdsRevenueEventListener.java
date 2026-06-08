package haui.foxtrip.review.service.util;

import haui.foxtrip.location.service.event.AdsRevenueEvent;
import haui.foxtrip.review.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdsRevenueEventListener {

    private final RevenueReportService revenueReportService;

    @EventListener
    public void handleAdsRevenueEvent(AdsRevenueEvent event) {
        log.info("Received AdsRevenueEvent: month={}, year={}, amount={}", 
                event.getMonth(), event.getYear(), event.getAmount());
        
        try {
            revenueReportService.incrementAdsRevenue(
                event.getMonth(), 
                event.getYear(), 
                event.getAmount()
            );
        } catch (Exception e) {
            log.error("Failed to process AdsRevenueEvent", e);
        }
    }
}
