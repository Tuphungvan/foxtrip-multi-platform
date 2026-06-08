package haui.foxtrip.order.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.service.OrderServiceImpl;
import haui.foxtrip.order.service.RefundServiceImpl;
import haui.foxtrip.order.service.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
public class AdminOrderController {

    private final OrderServiceImpl orderService;
    private final RefundServiceImpl refundService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageData<AdminOrderListItemDTO>>> getAdminOrders(
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "customerEmail", required = false) String customerEmail,
            @RequestParam(name = "orderCode", required = false) String orderCode,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageData<AdminOrderListItemDTO> response = orderService.getAdminOrders(status, customerEmail, orderCode,
                pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", response));
    }

    @PostMapping("/orders/{orderId}/refunds")
    public ResponseEntity<ApiResponse<RefundResDTO>> createRefund(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody CreateRefundReqDTO request) {
        RefundResDTO response = refundService.createRefund(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo yêu cầu hoàn tiền thành công", response));
    }

    @PatchMapping("/refunds/{refundId}")
    public ResponseEntity<ApiResponse<RefundResDTO>> updateRefund(
            @PathVariable("refundId") UUID refundId,
            @Valid @RequestBody UpdateRefundReqDTO request) {
        RefundResDTO response = refundService.updateRefund(refundId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật yêu cầu hoàn tiền thành công", response));
    }

    @PostMapping("/orders/{orderId}/reject-cancellation")
    public ResponseEntity<ApiResponse<Void>> rejectCancellation(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody CancelOrderReqDTO request) {
        orderService.rejectCancellationRequest(orderId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối yêu cầu hủy đơn hàng", null));
    }

    @PatchMapping("/tours/{tourId}/terminate")
    public ResponseEntity<ApiResponse<Void>> terminateTour(@PathVariable("tourId") UUID tourId) {
        orderService.terminateTour(tourId);
        return ResponseEntity.ok(ApiResponse.success("Kết thúc tour thành công", null));
    }
}
