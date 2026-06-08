package haui.foxtrip.cart.service;

import org.springframework.http.HttpStatus;

import haui.foxtrip.cart.domain.Cart;
import haui.foxtrip.cart.domain.CartItem;
import haui.foxtrip.cart.repository.CartItemRepository;
import haui.foxtrip.cart.repository.CartRepository;
import haui.foxtrip.cart.service.CartService;
import haui.foxtrip.cart.service.dto.CartResDTO;
import haui.foxtrip.cart.service.dto.UpsertCartItemReqDTO;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.cart.service.mapper.CartMapper;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.repository.TourAddonRepository;
import haui.foxtrip.tour.repository.TourRepository;
import haui.foxtrip.tour.service.mapper.TourMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TourRepository tourRepository;
    private final TourAddonRepository tourAddonRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResDTO getCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        if (cart == null) {
            return CartResDTO.builder().items(List.of()).build();
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            return CartResDTO.builder().items(List.of()).build();
        }

        // Lấy danh sách tour IDs
        List<UUID> tourIds = cartItems.stream().map(CartItem::getTourId).collect(Collectors.toList());

        // Query tours (chỉ lấy tour chưa bị xóa hẳn khỏi DB)
        List<Tour> tours = tourRepository.findAllById(tourIds).stream()
                .filter(tour -> tour.getDeletedAt() == null)
                .collect(Collectors.toList());

        Map<UUID, Tour> tourMap = tours.stream().collect(Collectors.toMap(Tour::getId, tour -> tour));

        // Tự động làm sạch giỏ hàng: Xóa các item không còn khả dụng
        Instant now = Instant.now();
        List<CartItem> validCartItems = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Tour tour = tourMap.get(item.getTourId());
            boolean isInvalid = false;

            if (tour == null) {
                // Tour đã bị xóa hẳn khỏi DB
                isInvalid = true;
            } else if (tour.getStatus() != TourStatus.ACTIVE) {
                // Tour không còn ở trạng thái ACTIVE (Ngưng/Ẩn/Hoàn thành)
                isInvalid = true;
            } else if (tour.getStartDate().isBefore(now)) {
                // Tour đã khởi hành
                isInvalid = true;
            }

            if (isInvalid) {
                log.info("Auto-cleanup: Removing invalid cart item {} for user {}", item.getTourId(), userId);
                cartItemRepository.deleteByCartIdAndTourId(cart.getId(), item.getTourId());
            } else {
                validCartItems.add(item);
            }
        }

        if (validCartItems.isEmpty()) {
            return CartResDTO.builder().items(List.of()).build();
        }

        // Lấy danh sách tour IDs của các item còn hiệu lực
        List<UUID> validTourIds = validCartItems.stream().map(CartItem::getTourId).collect(Collectors.toList());

        // Lấy danh sách addons khả dụng cho các tours này
        List<TourAddon> allAddons = tourAddonRepository.findByTourIdIn(validTourIds).stream()
                .filter(addon -> Boolean.TRUE.equals(addon.getIsActive()))
                .collect(Collectors.toList());

        Map<UUID, List<TourAddon>> addonMap = allAddons.stream()
                .collect(Collectors.groupingBy(TourAddon::getTourId));

        // Map to DTO
        List<CartResDTO.CartItemDTO> items = validCartItems.stream()
                .map(item -> {
                    Tour tour = tourMap.get(item.getTourId());
                    List<TourAddon> tourAddons = addonMap.getOrDefault(tour.getId(), List.of());

                    CartResDTO.CartItemDTO itemDto = cartMapper.toItemDto(item);
                    CartResDTO.TourInfoDTO tourInfoDto = cartMapper.toTourInfoDto(tour);

                    tourInfoDto.setAddons(tourAddons.stream()
                            .map(cartMapper::toAddonDto)
                            .collect(Collectors.toList()));

                    itemDto.setTour(tourInfoDto);
                    return itemDto;
                })
                .collect(Collectors.toList());

        return CartResDTO.builder().items(items).build();
    }

    @Override
    @Transactional
    public CartResDTO upsertCartItem(UUID userId, UpsertCartItemReqDTO request) {
        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return cartRepository.save(newCart);
        });

        // Nếu quantity = 0 thì xóa item (Luôn cho phép xóa kể cả tour đã ngưng)
        if (request.getQuantity() == 0) {
            cartItemRepository.deleteByCartIdAndTourId(cart.getId(), request.getTourId());
            return getCart(userId);
        }

        // Validate tour exists
        Tour tour = tourRepository
                .findByIdAndDeletedAtIsNull(request.getTourId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không tồn tại"));

        // Validate tour bookable
        if (tour.getStatus() != TourStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour hiện không khả dụng để đặt");
        }
        if (tour.getStartDate().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour đã khởi hành");
        }

        // Validate quantity
        if (request.getQuantity() > tour.getAvailableSlots()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số lượng vượt quá số chỗ còn lại (" + tour.getAvailableSlots() + ")");
        }

        // Upsert cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndTourId(cart.getId(), request.getTourId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCartId(cart.getId());
                    newItem.setTourId(request.getTourId());
                    return newItem;
                });

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
        }
    }
}
