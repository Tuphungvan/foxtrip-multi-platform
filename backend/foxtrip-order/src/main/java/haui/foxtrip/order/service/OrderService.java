package haui.foxtrip.order.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.service.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import haui.foxtrip.order.domain.Order;

public interface OrderService {

    CreateOrderResDTO createUserOrder(CreateOrderReqDTO request);

    PaymentInitResDTO initializePayment(UUID orderId, String ipAddress);

    String processVNPayIPN(Map<String, String> vnpParams);

    String processVNPayReturn(Map<String, String> vnpParams);

    PageData<MyOrderListItemDTO> getMyOrders(OrderStatus status, Pageable pageable);

    OrderDetailResDTO getOrderDetail(UUID orderId);

    void requestCancelOrder(UUID orderId, CancelOrderReqDTO request);

    void completeOrdersForTour(UUID tourId, String tourName);

    void markRefundPendingByTour(UUID tourId);

    PageData<AdminOrderListItemDTO> getAdminOrders(OrderStatus status, String customerEmail,
                                                   String orderCode, Pageable pageable);

    void terminateTour(UUID tourId);

    RevenueSplitDTO getRevenueSplitByTour(UUID tourId);

    void processTourCompletion(UUID tourId, String tourName);

    List<PassengerResDTO> getPassengersByTour(UUID tourId);

    void checkIn(UUID tourId, String orderCode);

    void rejectCancellationRequest(UUID orderId, String reason);

    List<Order> getPaidOrdersByTourId(UUID tourId);
}
