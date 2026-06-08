package haui.foxtrip.order.service.messaging;

import haui.foxtrip.order.service.event.OrderPaidEvent;
import haui.foxtrip.order.service.event.TourTerminatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${foxtrip.rabbitmq.exchange}")
    private String exchange;

    @Value("${foxtrip.rabbitmq.routing-keys.payment-success}")
    private String paymentSuccessRoutingKey;

    @Value("${foxtrip.rabbitmq.routing-keys.tour-terminated}")
    private String tourTerminatedRoutingKey;

    public void sendOrderPaidEvent(UUID orderId, String orderCode) {
        OrderPaidEvent event = new OrderPaidEvent(orderId, orderCode);
        log.info("Sending OrderPaidEvent to RabbitMQ for order: {}", orderCode);
        rabbitTemplate.convertAndSend(exchange, paymentSuccessRoutingKey, event);
    }

    public void sendTourTerminatedEvent(UUID tourId, String tourName, boolean isEarlyTermination) {
        TourTerminatedEvent event = new TourTerminatedEvent(tourId, tourName, isEarlyTermination);
        log.info("Sending TourTerminatedEvent to RabbitMQ for tour: {} (Early: {})", tourName, isEarlyTermination);
        rabbitTemplate.convertAndSend(exchange, tourTerminatedRoutingKey, event);
    }
}
