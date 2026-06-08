package haui.foxtrip.order.service.messaging;

import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.repository.OrderRepository;
import haui.foxtrip.order.service.OrderService;
import haui.foxtrip.order.service.event.OrderPaidEvent;
import haui.foxtrip.order.service.event.TourTerminatedEvent;
import haui.foxtrip.order.service.util.EmailService;
import haui.foxtrip.order.service.util.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final QRCodeService qrCodeService;
    private final EmailService emailService;
    private final OrderService orderService;

    @RabbitListener(queues = "${foxtrip.rabbitmq.queues.payment-success}")
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        log.info("Received OrderPaidEvent for order: {}", event.getOrderCode());
        try {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            // 1. Generate QR Code
            String qrBase64 = qrCodeService.generate(order.getOrderCode());

            // 2. Send Email
            emailService.sendOrderConfirmationEmail(order, qrBase64);
            
            log.info("Successfully processed post-payment tasks for order: {}", event.getOrderCode());
        } catch (Exception e) {
            log.error("Error processing OrderPaidEvent for order: {}", event.getOrderCode(), e);
            // In a real system, we might throw an exception here to trigger RabbitMQ retry
            throw new RuntimeException("Failed to process order paid event", e);
        }
    }

    @RabbitListener(queues = "${foxtrip.rabbitmq.queues.tour-terminated}")
    public void handleTourTerminatedEvent(TourTerminatedEvent event) {
        log.info("Received TourTerminatedEvent for tour: {} (Early: {})", event.getTourName(), event.isEarlyTermination());
        try {
            if (event.isEarlyTermination()) {
                orderService.markRefundPendingByTour(event.getTourId());
            } else {
                orderService.completeOrdersForTour(event.getTourId(), event.getTourName());
            }
            log.info("Successfully processed tour termination tasks for tour: {}", event.getTourName());
        } catch (Exception e) {
            log.error("Error processing TourTerminatedEvent for tour: {}", event.getTourName(), e);
            throw new RuntimeException("Failed to process tour terminated event", e);
        }
    }
}
