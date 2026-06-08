package haui.foxtrip.review.service.util;

import haui.foxtrip.order.service.event.OrderRevenueEvent;
import haui.foxtrip.review.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRevenueEventListener {

    private final RevenueReportService revenueReportService;

    @EventListener
    public void handleOrderRevenueEvent(OrderRevenueEvent event) {
        log.info("Received OrderRevenueEvent: month={}, year={}, tourAmount={}, addonAmount={}, count={}", 
                event.getMonth(), event.getYear(), event.getTourAmount(), event.getAddonAmount(), event.getOrderCount());
        
        try {
            revenueReportService.incrementRevenue(
                event.getMonth(), 
                event.getYear(), 
                event.getTourAmount(),
                event.getAddonAmount(),
                event.getOrderCount()
            );
        } catch (Exception e) {
            log.error("Failed to update revenue for event: {}", event, e);
        }
    }
}
