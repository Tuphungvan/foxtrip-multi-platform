package haui.foxtrip.cart.web.rest;

import haui.foxtrip.cart.service.CartService;
import haui.foxtrip.cart.service.dto.CartResDTO;
import haui.foxtrip.cart.service.dto.UpsertCartItemReqDTO;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.common.util.SecurityUtils;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResDTO>> getCart() {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        CartResDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công", cart));
    }

    @PutMapping("/item")
    public ResponseEntity<ApiResponse<CartResDTO>> upsertCartItem(@Valid @RequestBody UpsertCartItemReqDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        CartResDTO cart = cartService.upsertCartItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giỏ hàng thành công", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa giỏ hàng thành công", null));
    }
}
