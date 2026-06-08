package haui.foxtrip.cart.service;

import haui.foxtrip.cart.service.dto.CartResDTO;
import haui.foxtrip.cart.service.dto.UpsertCartItemReqDTO;
import java.util.UUID;

public interface CartService {

    CartResDTO getCart(UUID userId);

    CartResDTO upsertCartItem(UUID userId, UpsertCartItemReqDTO request);

    void clearCart(UUID userId);
}
