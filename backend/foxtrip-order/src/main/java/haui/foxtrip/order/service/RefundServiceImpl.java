package haui.foxtrip.order.service;

import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.domain.OrderItem;
import haui.foxtrip.order.domain.Refund;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.domain.enums.RefundStatus;
import haui.foxtrip.order.repository.OrderItemRepository;
import haui.foxtrip.order.repository.OrderRepository;
import haui.foxtrip.order.repository.RefundRepository;
import haui.foxtrip.order.service.dto.CreateRefundReqDTO;
import haui.foxtrip.order.service.dto.RefundResDTO;
import haui.foxtrip.order.service.dto.UpdateRefundReqDTO;
import haui.foxtrip.order.service.mapper.OrderMapper;
import haui.foxtrip.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import haui.foxtrip.order.service.event.OrderRevenueEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TourRepository tourRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RefundResDTO createRefund(UUID orderId, CreateRefundReqDTO request) {
        UUID adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.CANCEL_REQUESTED && order.getStatus() != OrderStatus.REFUND_PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng không ở trạng thái yêu cầu hủy hoặc chờ hoàn tiền");
        }

        refundRepository.findByOrderId(orderId).ifPresent(existingRefund -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng đã có yêu cầu hoàn tiền");
        });

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setAmount(request.getAmount());
        refund.setStatus(RefundStatus.PENDING);
        refund.setReason(request.getReason());

        refund = refundRepository.save(refund);

        order.setStatus(OrderStatus.REFUND_PENDING);
        orderRepository.save(order);

        // CỘNG DỒN 50% doanh thu (Phí hủy tour) vào báo cáo (Phát sự kiện)
        BigDecimal penaltyAmount = order.getTotalAmount().subtract(request.getAmount());
        if (penaltyAmount.compareTo(BigDecimal.ZERO) > 0) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            eventPublisher.publishEvent(new OrderRevenueEvent(now.getMonthValue(), now.getYear(), penaltyAmount, BigDecimal.ZERO, 0));
        }

        log.info("Admin {} created refund for order: {}. Penalty revenue: {}", adminId, order.getOrderCode(),
                penaltyAmount);

        return orderMapper.toRefundResDTO(refund);
    }

    @Transactional
    public RefundResDTO updateRefund(UUID refundId, UpdateRefundReqDTO request) {
        UUID adminId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Yêu cầu hoàn tiền không tồn tại"));

        Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Đơn hàng không tồn tại"));

        refund.setStatus(request.getStatus());
        refund.setProcessedBy(adminId);
        refund.setProcessedAt(Instant.now());

        if (request.getNote() != null) {
            refund.setReason(refund.getReason() + " | Admin note: " + request.getNote());
        }

        refund = refundRepository.save(refund);

        if (request.getStatus() == RefundStatus.COMPLETED) {
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            if (!orderItems.isEmpty()) {
                OrderItem orderItem = orderItems.get(0);
                tourRepository.incrementSlots(orderItem.getTourId(), orderItem.getQuantity());
                log.info("Restored {} slots for tour: {}", orderItem.getQuantity(), orderItem.getTourId());
            }
        }

        log.info("Admin {} updated refund {} to status: {}", adminId, refundId, request.getStatus());

        return orderMapper.toRefundResDTO(refund);
    }
}
