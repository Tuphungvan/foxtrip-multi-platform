package haui.foxtrip.cart.web.rest;

import haui.foxtrip.cart.service.CheckoutService;
import haui.foxtrip.cart.service.dto.CheckoutPreviewReqDTO;
import haui.foxtrip.cart.service.dto.CheckoutPreviewResDTO;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.common.util.SecurityUtils;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<CheckoutPreviewResDTO>> previewCheckout(
            @Valid @RequestBody CheckoutPreviewReqDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        CheckoutPreviewResDTO preview = checkoutService.previewCheckout(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Tính toán thành công", preview));
    }
}
