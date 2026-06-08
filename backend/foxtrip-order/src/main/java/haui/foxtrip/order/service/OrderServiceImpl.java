package haui.foxtrip.order.service;

import org.springframework.http.HttpStatus;

import haui.foxtrip.cart.service.CartService;
import haui.foxtrip.cart.service.dto.CartResDTO;
import haui.foxtrip.cart.service.dto.UpsertCartItemReqDTO;
import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.tour.domain.enums.TourStatus;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import haui.foxtrip.order.config.OrderConfig;
import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.domain.OrderAddon;
import haui.foxtrip.order.domain.OrderItem;
import haui.foxtrip.order.domain.Refund;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.domain.enums.RefundStatus;
import haui.foxtrip.order.repository.OrderAddonRepository;
import haui.foxtrip.order.repository.OrderItemRepository;
import haui.foxtrip.order.repository.OrderRepository;
import haui.foxtrip.order.repository.RefundRepository;
import haui.foxtrip.order.service.dto.*;
import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.repository.UserRepository;
import haui.foxtrip.order.service.mapper.OrderMapper;
import haui.foxtrip.order.service.util.EmailService;
import haui.foxtrip.order.service.util.OrderCodeGenerator;
import haui.foxtrip.order.service.util.QRCodeService;
import haui.foxtrip.order.service.util.VNPayService;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.repository.TourAddonRepository;
import haui.foxtrip.tour.repository.TourRepository;
import haui.foxtrip.order.service.messaging.OrderEventProducer;
import haui.foxtrip.order.service.event.OrderRevenueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddonRepository orderAddonRepository;
    private final UserRepository userRepository;
    private final RefundRepository refundRepository;
    private final TourRepository tourRepository;
    private final TourAddonRepository tourAddonRepository;
    private final OrderCodeGenerator orderCodeGenerator;
    private final OrderConfig orderConfig;
    private final QRCodeService qrCodeService;
    private final VNPayService vnPayService;
    private final EmailService emailService;
    private final CartService cartService;
    private final OrderEventProducer orderEventProducer;
    private final OrderMapper orderMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    private static final String CHECK_IN_CACHE = "tour-check-in";

    private static final int MAX_ORDER_CODE_RETRY = 3;

    /**
     * Create user order (USER)
     */
    @Transactional
    public CreateOrderResDTO createUserOrder(CreateOrderReqDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Người dùng không tồn tại"));

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "EMAIL_NOT_VERIFIED");
        }

        // Validate tour
        Tour tour = tourRepository.findByIdAndDeletedAtIsNull(request.getTourId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không tồn tại"));

        // Determine quantity
        Integer quantity = request.getFromCart() ? getQuantityFromCart(userId, request.getTourId())
                : request.getQuantity();
        if (quantity == null || quantity < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số lượng không hợp lệ");
        }

        validateTourBookable(tour, quantity);

        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(tour, quantity, request.getAddons());

        // Generate order code with retry
        String orderCode = generateUniqueOrderCode();

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode(orderCode);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setExpiresAt(Instant.now().plus(orderConfig.getExpirationHours(), ChronoUnit.HOURS));

        order = orderRepository.save(order);

        // Create order item (snapshot)
        createOrderItem(order, tour, quantity);

        // Create order addons (snapshot)
        if (request.getAddons() != null && !request.getAddons().getItems().isEmpty()) {
            createOrderAddons(order, request.getAddons().getItems());
        }

        // Clear cart if fromCart
        if (request.getFromCart()) {
            clearCartItem(userId, request.getTourId());
        }

        log.info("Created user order: {} for user: {} tour: {}", orderCode, userId, tour.getName());

        return orderMapper.toCreateOrderResDTO(order);
    }

    /**
     * Initialize payment (PUBLIC/USER)
     */
    @Transactional(readOnly = true)
    public PaymentInitResDTO initializePayment(UUID orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Đơn hàng không tồn tại"));

        // Validate order status
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // Ownership check: Only the owner or an admin/guide can initialize payment
        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));

        if (!currentUserId.equals(order.getUserId()) && !isAdmin()) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền thanh toán đơn hàng này");
        }

        // Check expiration
        if (Instant.now().isAfter(order.getExpiresAt())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng đã hết hạn");
        }

        // Generate VNPay payment URL
        String orderInfo = "Thanh toan tour - " + order.getOrderCode();
        String paymentUrl = vnPayService.generatePaymentUrl(
                order.getOrderCode(),
                order.getTotalAmount(),
                orderInfo,
                ipAddress);

        return orderMapper.toPaymentInitResDTO(order, paymentUrl);
    }

    /**
     * Process VNPay IPN (PUBLIC)
     * 
     * @return VNPay Response Code (RspCode)
     */
    @Override
    @Transactional
    public String processVNPayIPN(Map<String, String> vnpParams) {
        return handleVNPayPaymentResult(vnpParams);
    }

    @Override
    @Transactional
    public String processVNPayReturn(Map<String, String> vnpParams) {
        return handleVNPayPaymentResult(vnpParams);
    }

    private String handleVNPayPaymentResult(Map<String, String> vnpParams) {
        // Verify signature
        if (!vnPayService.verifyIpnSignature(vnpParams)) {
            log.error("VNPay payment signature verification failed");
            return "97"; // Invalid signature
        }

        String rawTxnRef = vnpParams.get("vnp_TxnRef");
        // Extract real orderCode (remove the -timestamp suffix)
        String orderCode = rawTxnRef != null && rawTxnRef.contains("-") 
                ? rawTxnRef.substring(0, rawTxnRef.lastIndexOf("-")) 
                : rawTxnRef;
        
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String txnRef = vnpParams.get("vnp_TransactionNo");

        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            log.error("VNPay payment: Order not found: {}", orderCode);
            return "01"; // Order not found
        }

        // Verify Amount
        try {
            long vnpAmount = Long.parseLong(vnpParams.get("vnp_Amount"));
            long expectedAmount = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
            if (vnpAmount != expectedAmount) {
                log.error("VNPay payment amount mismatch. Order: {}, Expected: {}, Got: {}", orderCode, expectedAmount,
                        vnpAmount);
                return "04"; // Invalid amount
            }
        } catch (Exception e) {
            log.error("VNPay payment amount format error for order: {}", orderCode, e);
            return "04"; // Invalid amount
        }

        // Check if already processed
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED) {
            log.info("Order {} already paid, skipping processing", orderCode);
            return "02"; // Order already confirmed
        }

        // Check response code from VNPAY
        if (!"00".equals(responseCode)) {
            log.warn("VNPay payment failed for order: {} with response code: {}", orderCode, responseCode);
            return responseCode;
        }

        // Get tour and quantity
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems.isEmpty()) {
            log.error("No order items found for order: {}", orderCode);
            return "99"; // Unknown error
        }

        OrderItem orderItem = orderItems.get(0);
        UUID tourId = orderItem.getTourId();
        int quantity = orderItem.getQuantity();

        // Atomic decrement slots
        int rowsAffected = tourRepository.decrementSlots(tourId, quantity);

        if (rowsAffected == 0) {
            // Tour sold out during payment
            log.warn("Tour {} sold out during payment for order: {}", tourId, orderCode);
            order.setStatus(OrderStatus.REFUND_PENDING);
            order.setStatusNote("Tour sold out during payment process");
            orderRepository.save(order);

            // Create refund request
            Refund refund = new Refund();
            refund.setOrderId(order.getId());
            refund.setAmount(order.getTotalAmount());
            refund.setStatus(RefundStatus.PENDING);
            refund.setReason("Tour hết chỗ trong quá trình thanh toán");
            refundRepository.save(refund);

            log.info("Created refund request for sold-out order: {}", orderCode);
            return "00"; 
        }

        // Update order status
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());
        order.setVnpayTxnRef(txnRef);
        order.setStatusNote(null);
        orderRepository.save(order);

        log.info("Order {} paid successfully, amount: {}", orderCode, order.getTotalAmount());

        // Publish event to RabbitMQ for QR and Email
        orderEventProducer.sendOrderPaidEvent(order.getId(), order.getOrderCode());

        return "00";
    }

    /**
     * Get my orders (USER)
     */
    @Transactional(readOnly = true)
    public PageData<MyOrderListItemDTO> getMyOrders(OrderStatus status, Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));

        Page<Order> orderPage = status == null
                ? orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : orderRepository.findByUserIdAndStatus(userId, status, pageable);

        List<Order> orders = orderPage.getContent();
        List<UUID> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<UUID, List<OrderItem>> orderItemsMap = orderIds.isEmpty() ? Map.of()
                : orderItemRepository.findAllByOrderIdIn(orderIds).stream()
                        .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<MyOrderListItemDTO> items = orders.stream()
                .map(order -> mapToMyOrderListItem(order, orderItemsMap))
                .collect(Collectors.toList());

        return new PageData<>(
                items,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages());
    }

    /**
     * Get order detail (USER/ADMIN/GUIDE)
     */
    @Transactional(readOnly = true)
    public OrderDetailResDTO getOrderDetail(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Đơn hàng không tồn tại"));

        // Security & Role check
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        boolean admin = isAdmin();

        // 1. If Admin/SuperAdmin: Allow all
        if (admin) {
            return mapToOrderDetail(order);
        }

        // 2. Access check: Must be owner or associated guide
        if (currentUserId == null
                || (!currentUserId.equals(order.getUserId()) && !isGuideOfOrder(order, currentUserId))) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem đơn hàng này");
        }

        return mapToOrderDetail(order);
    }

    /**
     * Request cancel order (USER)
     */
    @Transactional
    public void requestCancelOrder(UUID orderId, CancelOrderReqDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Đơn hàng không tồn tại"));

        // Check ownership
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền hủy đơn hàng này");
        }

        // Check status
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Chỉ có thể hủy đơn hàng đã thanh toán");
        }

        // Check 24h deadline before tour starts
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (!items.isEmpty()) {
            Tour tour = tourRepository.findById(items.get(0).getTourId()).orElse(null);
            if (tour != null && Instant.now().plus(24, ChronoUnit.HOURS).isAfter(tour.getStartDate())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Không thể hủy đơn hàng khi sát ngày khởi hành (dưới 24h) hoặc đã khởi hành");
            }
        }

        order.setStatus(OrderStatus.CANCEL_REQUESTED);
        order.setStatusNote(request.getReason());
        orderRepository.save(order);

        log.info("User {} requested to cancel order: {}", userId, order.getOrderCode());
    }

    /**
     * Mark all PAID orders for a tour as COMPLETED and send review nudge emails
     * (for Lifecycle Job)
     */
    @Transactional
    public void completeOrdersForTour(UUID tourId, String tourName) {
        List<Order> paidOrders = orderRepository.findByTourIdAndStatus(tourId, OrderStatus.PAID);

        if (paidOrders.isEmpty()) {
            return;
        }

        orderRepository.updateStatusByTourId(tourId, OrderStatus.COMPLETED.name());
        log.info("Transitioned {} PAID orders for tour {} to COMPLETED", paidOrders.size(), tourId);

        // Send review nudge emails asynchronously
        for (Order order : paidOrders) {
            emailService.sendReviewNudgeEmail(order, tourName);
        }
    }

    /**
     * Mark all PAID orders as REFUND_PENDING and create Refund records (Early
     * Termination)
     */
    @Transactional
    public void markRefundPendingByTour(UUID tourId) {
        List<Order> paidOrders = orderRepository.findByTourIdAndStatus(tourId, OrderStatus.PAID);
        if (paidOrders.isEmpty()) {
            return;
        }

        for (Order order : paidOrders) {
            order.setStatus(OrderStatus.REFUND_PENDING);
            orderRepository.save(order);

            Refund refund = new Refund();
            refund.setOrderId(order.getId());
            refund.setAmount(order.getTotalAmount());
            refund.setStatus(RefundStatus.PENDING);
            refund.setReason("Tour kết thúc sớm do Admin chủ động dừng");
            refundRepository.save(refund);
        }
        log.info("Transitioned {} PAID orders for tour {} to REFUND_PENDING and created Refund records",
                paidOrders.size(), tourId);
    }

    /**
     * Get admin orders (ADMIN)
     */
    @Transactional(readOnly = true)
    public PageData<AdminOrderListItemDTO> getAdminOrders(OrderStatus status, String customerEmail,
            String orderCode, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByAdminFilters(status, customerEmail, orderCode, pageable);

        List<Order> orders = orderPage.getContent();
        List<UUID> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<UUID, List<OrderItem>> orderItemsMap = orderIds.isEmpty() ? Map.of()
                : orderItemRepository.findAllByOrderIdIn(orderIds).stream()
                        .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<AdminOrderListItemDTO> items = orders.stream()
                .map(order -> mapToAdminOrderListItem(order, orderItemsMap))
                .collect(Collectors.toList());

        return new PageData<>(
                items,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages());
    }

    /**
     * Terminate a tour manually (ADMIN)
     * Transitions Tour to COMPLETED and handles orders/refunds based on timing.
     */
    @Transactional
    public void terminateTour(UUID tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour"));

        if (tour.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.GONE.value(), "Tour đã bị xóa");
        }

        if (tour.getStatus() != TourStatus.ACTIVE &&
                tour.getStatus() != TourStatus.ONGOING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Chỉ có thể kết thúc tour đang hoạt động hoặc diễn ra");
        }

        tour.setStatus(TourStatus.COMPLETED);
        tourRepository.save(tour);

        if (Instant.now().isBefore(tour.getEndDate())) {
            // Early Termination -> Bulk processing via RabbitMQ
            orderEventProducer.sendTourTerminatedEvent(tourId, tour.getName(), true);
        } else {
            // Standard Completion -> Bulk processing via RabbitMQ
            orderEventProducer.sendTourTerminatedEvent(tourId, tour.getName(), false);
        }
    }

    @Override
    @Transactional
    public void processTourCompletion(UUID tourId, String tourName) {
        orderEventProducer.sendTourTerminatedEvent(tourId, tourName, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerResDTO> getPassengersByTour(UUID tourId) {
        List<PassengerResDTO> passengers = orderRepository.findPassengersByTourId(tourId);
        org.springframework.cache.Cache cache = cacheManager.getCache(CHECK_IN_CACHE);
        if (cache != null) {
            for (PassengerResDTO p : passengers) {
                String cacheKey = tourId + ":" + p.getOrderCode();
                p.setCheckedIn(cache.get(cacheKey) != null);
            }
        }
        return passengers;
    }

    @Override
    @Transactional
    public void checkIn(UUID tourId, String qrPayload) {
        String orderCode;
        
        // 1. Determine if it's a signed QR payload or a manual orderCode
        if (qrPayload.contains("|")) {
            // Case: Scanned QR code -> Verify HMAC signature
            orderCode = qrCodeService.verify(qrPayload);
        } else {
            // Case: Manual entry -> Fallback for guides
            orderCode = qrPayload;
            
            // Check if requester is the assigned guide for this tour
            UUID currentUserId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));
            
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không tồn tại"));
            
            if (!currentUserId.equals(tour.getGuideId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện check-in thủ công cho tour này");
            }
            
            log.info("Guide {} performing manual check-in for order {}", currentUserId, orderCode);
        }

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Mã đơn hàng không tồn tại"));

        // Check if this order contains the specified tour
        boolean belongsToTour = orderItemRepository.existsByOrderIdAndTourId(order.getId(), tourId);
        if (!belongsToTour) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng này không thuộc Tour hiện tại");
        }

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng chưa thanh toán hoặc ở trạng thái không hợp lệ: " + order.getStatus());
        }

        // Cache-based Check-in logic
        Cache cache = cacheManager.getCache(CHECK_IN_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Check-in cache not configured");
        }

        String cacheKey = tourId + ":" + orderCode;
        Cache.ValueWrapper existing = cache.putIfAbsent(cacheKey, Boolean.TRUE);
        if (existing != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Mã này đã được quét hoặc check-in trước đó");
        }

        log.info("Check-in successful for order {} tour {}", orderCode, tourId);
    }

    @Override
    @Transactional
    public void rejectCancellationRequest(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.CANCEL_REQUESTED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Đơn hàng không ở trạng thái yêu cầu hủy");
        }

        // Kiểm tra xem tour đã kết thúc chưa. Nếu kết thúc rồi thì chuyển thẳng sang COMPLETED
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        boolean isTourFinished = false;
        if (!items.isEmpty()) {
            Tour tour = tourRepository.findById(items.get(0).getTourId()).orElse(null);
            if (tour != null && tour.getEndDate().isBefore(Instant.now())) {
                isTourFinished = true;
            }
        }

        if (isTourFinished) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setStatusNote("Từ chối hủy sau khi tour đã kết thúc: " + reason);
            orderRepository.save(order);
            
            // Phát sự kiện để module review tự cập nhật doanh thu (Tránh phụ thuộc vòng)
            Tour tour = tourRepository.findById(items.get(0).getTourId()).get();
            ZonedDateTime zdt = tour.getEndDate().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            
            // Calculate tour and addon split for this specific order
            BigDecimal tourAmount = items.stream()
                .map(OrderItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal addonAmount = orderAddonRepository.findByOrderId(orderId).stream()
                .map(oa -> oa.getPriceAtTime().multiply(BigDecimal.valueOf(oa.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            eventPublisher.publishEvent(new OrderRevenueEvent(
                zdt.getMonthValue(), zdt.getYear(), tourAmount, addonAmount, 1
            ));
        } else {
            order.setStatus(OrderStatus.PAID);
            order.setStatusNote("Từ chối hủy: " + reason);
            orderRepository.save(order);
        }

        emailService.sendCancellationRejectionEmail(order, reason);
        log.info("Admin rejected cancellation request for order: {}. Target status: {}. Reason: {}", 
                order.getOrderCode(), order.getStatus(), reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getPaidOrdersByTourId(UUID tourId) {
        return orderRepository.findByTourIdAndStatus(tourId, OrderStatus.PAID);
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueSplitDTO getRevenueSplitByTour(UUID tourId) {
        List<Order> orders = orderRepository.findByTourIdAndStatus(tourId, OrderStatus.PAID);
        if (orders.isEmpty()) {
            return new RevenueSplitDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        List<UUID> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        
        BigDecimal tourTotal = orderItemRepository.findAllByOrderIdIn(orderIds).stream()
            .filter(item -> item.getTourId().equals(tourId))
            .map(OrderItem::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal addonTotal = orderAddonRepository.findAllByOrderIdIn(orderIds).stream()
            .map(oa -> oa.getPriceAtTime().multiply(BigDecimal.valueOf(oa.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RevenueSplitDTO(tourTotal, addonTotal, orders.size());
    }

    // ========== Private Helper Methods ==========

    private void validateTourBookable(Tour tour, int quantity) {
        if (tour.getStatus() != TourStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour hiện không khả dụng để đặt");
        }

        if (tour.getAvailableSlots() < quantity) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour không đủ chỗ trống");
        }

        if (tour.getStartDate().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour đã khởi hành");
        }
    }

    private BigDecimal calculateTotalAmount(Tour tour, int quantity, AddonsWrapperDTO addonsWrapper) {
        BigDecimal tourPrice = tour.getPrice().multiply(BigDecimal.valueOf(100).subtract(tour.getDiscount()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = tourPrice.multiply(new BigDecimal(quantity));

        if (addonsWrapper != null && !addonsWrapper.getItems().isEmpty()) {
            List<UUID> addonIds = addonsWrapper.getItems().stream()
                    .map(AddonItemDTO::getTourAddonId)
                    .collect(Collectors.toList());

            Map<UUID, TourAddon> addonsMap = tourAddonRepository.findAllById(addonIds).stream()
                    .collect(Collectors.toMap(TourAddon::getId, addon -> addon));

            for (AddonItemDTO addonItem : addonsWrapper.getItems()) {
                TourAddon addon = addonsMap.get(addonItem.getTourAddonId());
                if (addon == null) {
                    throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Add-on không tồn tại");
                }
                if (!addon.getIsActive()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Add-on không còn khả dụng");
                }
                totalAmount = totalAmount.add(addon.getPrice().multiply(new BigDecimal(addonItem.getQuantity())));
            }
        }

        return totalAmount;
    }

    private String generateUniqueOrderCode() {
        for (int i = 0; i < MAX_ORDER_CODE_RETRY; i++) {
            String orderCode = orderCodeGenerator.generate();
            if (!orderRepository.existsByOrderCode(orderCode)) {
                return orderCode;
            }
        }
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Không thể tạo mã đơn hàng");
    }

    private void createOrderItem(Order order, Tour tour, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setTourId(tour.getId());
        orderItem.setQuantity(quantity);
        orderItem.setTourNameAtTime(tour.getName());
        orderItem.setStartDateAtTime(tour.getStartDate());
        orderItem.setEndDateAtTime(tour.getEndDate());
        orderItem.setThumbnailAtTime(tour.getThumbnailUrl());
        orderItem.setPriceAtTime(tour.getPrice());
        orderItem.setDiscountAtTime(tour.getDiscount());
        BigDecimal tourPrice = tour.getPrice().multiply(BigDecimal.valueOf(100).subtract(tour.getDiscount()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        orderItem.setFinalPrice(tourPrice.multiply(new BigDecimal(quantity)));

        orderItemRepository.save(orderItem);
    }

    private void createOrderAddons(Order order, List<AddonItemDTO> addonItems) {
        List<UUID> addonIds = addonItems.stream()
                .map(AddonItemDTO::getTourAddonId)
                .collect(Collectors.toList());

        Map<UUID, TourAddon> addonsMap = tourAddonRepository.findAllById(addonIds).stream()
                .collect(Collectors.toMap(TourAddon::getId, addon -> addon));

        List<OrderAddon> orderAddons = new ArrayList<>();
        for (AddonItemDTO addonItem : addonItems) {
            TourAddon addon = addonsMap.get(addonItem.getTourAddonId());
            if (addon != null) {
                OrderAddon orderAddon = new OrderAddon();
                orderAddon.setOrderId(order.getId());
                orderAddon.setTourAddonId(addon.getId());
                orderAddon.setQuantity(addonItem.getQuantity());
                orderAddon.setPriceAtTime(addon.getPrice());
                orderAddons.add(orderAddon);
            }
        }

        orderAddonRepository.saveAll(orderAddons);
    }

    private Integer getQuantityFromCart(UUID userId, UUID tourId) {
        CartResDTO cart = cartService.getCart(userId);
        if (cart == null || cart.getItems() == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Giỏ hàng trống");
        }

        return cart.getItems().stream()
                .filter(item -> item.getTourId().equals(tourId))
                .map(CartResDTO.CartItemDTO::getQuantity)
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không có trong giỏ hàng"));
    }

    private void clearCartItem(UUID userId, UUID tourId) {
        UpsertCartItemReqDTO request = new UpsertCartItemReqDTO();
        request.setTourId(tourId);
        request.setQuantity(0); // Removing item
        cartService.upsertCartItem(userId, request);
        log.info("Cleared cart item for user: {} tour: {}", userId, tourId);
    }

    private MyOrderListItemDTO mapToMyOrderListItem(Order order, Map<UUID, List<OrderItem>> orderItemsMap) {
        List<OrderItem> orderItems = orderItemsMap.getOrDefault(order.getId(), List.of());
        OrderItem orderItem = orderItems.isEmpty() ? null : orderItems.get(0);

        if (orderItem == null) {
            // Fallback if no order item
            return MyOrderListItemDTO.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .expiresAt(order.getExpiresAt())
                    .paidAt(order.getPaidAt())
                    .quantity(0)
                    .build();
        }

        return orderMapper.toMyOrderListItemDTO(order, orderItem);
    }

    private OrderDetailResDTO mapToOrderDetail(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderAddon> orderAddons = orderAddonRepository.findByOrderId(order.getId());

        OrderItem orderItem = orderItems.isEmpty() ? null : orderItems.get(0);

        if (orderItem == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Không tìm thấy thông tin tour trong đơn hàng");
        }

        // Map base order detail
        OrderDetailResDTO dto = orderMapper.toOrderDetailResDTO(order, orderItem);

        // Get addon names
        List<UUID> addonIds = orderAddons.stream()
                .map(OrderAddon::getTourAddonId)
                .collect(Collectors.toList());

        Map<UUID, String> addonNames = addonIds.isEmpty() ? Map.of()
                : tourAddonRepository.findAllById(addonIds).stream()
                        .collect(Collectors.toMap(TourAddon::getId, TourAddon::getName));

        // Map addons
        List<OrderDetailResDTO.AddonSnapshotDTO> addonSnapshots = orderAddons.stream()
                .map(addon -> orderMapper.toAddonSnapshotDTO(addon, addonNames.getOrDefault(addon.getTourAddonId(), "N/A")))
                .collect(Collectors.toList());

        dto.setAddons(addonSnapshots);

        // Populate refundId if exists (for admin to process refund)
        refundRepository.findTopByOrderIdOrderByCreatedAtDesc(order.getId())
            .ifPresent(refund -> dto.setRefundId(refund.getId()));

        // Generate QR payload if paid
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED) {
            dto.setQrPayload(qrCodeService.generatePayload(order.getOrderCode()));
        }

        return dto;
    }

    private AdminOrderListItemDTO mapToAdminOrderListItem(Order order, Map<UUID, List<OrderItem>> orderItemsMap) {
        List<OrderItem> orderItems = orderItemsMap.getOrDefault(order.getId(), List.of());
        OrderItem orderItem = orderItems.isEmpty() ? null : orderItems.get(0);

        if (orderItem == null) {
            // Fallback if no order item
            return AdminOrderListItemDTO.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .expiresAt(order.getExpiresAt())
                    .paidAt(order.getPaidAt())
                    .customerName(order.getCustomerName())
                    .customerPhone(order.getCustomerPhone())
                    .customerEmail(order.getCustomerEmail())
                    .tourNameAtTime("N/A")
                    .quantity(0)
                    .build();
        }

        return orderMapper.toAdminOrderListItemDTO(order, orderItem);
    }

    private boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ADMIN") || a.equals("SUPER_ADMIN"));
    }

    private boolean isGuideOfOrder(Order order, UUID currentUserId) {
        if (currentUserId == null)
            return false;

        // Check if user has GUIDE role first
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("GUIDE"))) {
            return false;
        }

        // Check if the tour in order belongs to this guide
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems.isEmpty())
            return false;

        OrderItem item = orderItems.get(0);
        return tourRepository.findById(item.getTourId())
                .map(tour -> currentUserId.equals(tour.getGuideId()))
                .orElse(false);
    }
}
