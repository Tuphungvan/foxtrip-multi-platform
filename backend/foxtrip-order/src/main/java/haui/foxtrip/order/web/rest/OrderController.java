package haui.foxtrip.order.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.service.OrderService;
import haui.foxtrip.order.service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 2.40 POST /api/orders - Create user order (USER)
     */
    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<CreateOrderResDTO>> createUserOrder(
            @Valid @RequestBody CreateOrderReqDTO request) {
        CreateOrderResDTO response = orderService.createUserOrder(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo đơn hàng thành công", response));
    }

    /**
     * 2.41 POST /api/orders/{orderId}/payment - Initialize payment (USER)
     */
    @PostMapping("/orders/{orderId}/payment")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<PaymentInitResDTO>> initializePayment(
            @PathVariable("orderId") UUID orderId,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        PaymentInitResDTO response = orderService.initializePayment(orderId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Khởi tạo thanh toán thành công", response));
    }

    /**
     * 2.43 GET /api/my/orders - Get my orders (USER)
     */
    @GetMapping("/my/orders")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<PageData<MyOrderListItemDTO>>> getMyOrders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        // Sắp xếp ổn định theo createdAt DESC và id ASC để tránh nhảy trang
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.ASC, "id")));
        PageData<MyOrderListItemDTO> response = orderService.getMyOrders(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", response));
    }

    /**
     * 2.44 GET /api/orders/{orderId} - Get order detail (USER/ADMIN/GUIDE)
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'SUPER_ADMIN', 'GUIDE')")
    public ResponseEntity<ApiResponse<OrderDetailResDTO>> getOrderDetail(@PathVariable("orderId") UUID orderId) {
        OrderDetailResDTO response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", response));
    }

    /**
     * 2.45 POST /api/orders/{orderId}/cancel-request - Request cancel order (USER)
     */
    @PostMapping("/orders/{orderId}/cancel-request")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<Void>> requestCancelOrder(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody CancelOrderReqDTO request) {
        orderService.requestCancelOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu hủy đơn hàng thành công"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
