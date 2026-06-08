package haui.foxtrip.cart.service;

import haui.foxtrip.cart.service.dto.CheckoutPreviewReqDTO;
import haui.foxtrip.cart.service.dto.CheckoutPreviewResDTO;
import java.util.UUID;

public interface CheckoutService {

    CheckoutPreviewResDTO previewCheckout(UUID userId, CheckoutPreviewReqDTO request);
}
