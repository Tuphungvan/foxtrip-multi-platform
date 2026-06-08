package haui.foxtrip.order.job;

import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationJob {

    private final OrderRepository orderRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void expirePendingOrders() {
        log.info("Starting order expiration job");

        List<Order> expiredOrders = orderRepository.findByStatusAndExpiresAtBefore(
                OrderStatus.PENDING,
                Instant.now());

        if (expiredOrders.isEmpty()) {
            log.info("No expired orders found");
            return;
        }

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            order.setStatusNote("Auto expired after 24h");
            log.info("Expired order: {}", order.getOrderCode());
        }

        orderRepository.saveAll(expiredOrders);

        log.info("Expired {} orders", expiredOrders.size());
    }
}
