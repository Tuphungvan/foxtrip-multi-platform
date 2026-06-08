package haui.foxtrip.cart.service;

import org.springframework.http.HttpStatus;

import haui.foxtrip.cart.domain.CartItem;
import haui.foxtrip.cart.repository.CartItemRepository;
import haui.foxtrip.cart.repository.CartRepository;
import haui.foxtrip.cart.service.dto.CheckoutPreviewReqDTO;
import haui.foxtrip.cart.service.dto.CheckoutPreviewResDTO;
import haui.foxtrip.cart.service.mapper.CartMapper;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.repository.TourAddonRepository;
import haui.foxtrip.tour.repository.TourRepository;
import java.math.BigDecimal;
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
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TourRepository tourRepository;
    private final TourAddonRepository tourAddonRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResDTO previewCheckout(UUID userId, CheckoutPreviewReqDTO request) {
        // Validate tour exists
        Tour tour = tourRepository
            .findByIdAndDeletedAtIsNull(request.getTourId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không tồn tại"));

        // Determine quantity
        Integer quantity;
        if (Boolean.TRUE.equals(request.getFromCart())) {
            // Lấy quantity từ cart
            var cart = cartRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Giỏ hàng trống"));

            CartItem cartItem = cartItemRepository
                .findByCartIdAndTourId(cart.getId(), request.getTourId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không có trong giỏ hàng"));

            quantity = cartItem.getQuantity();
        } else {
            // Lấy quantity từ request
            if (request.getQuantity() == null || request.getQuantity() < 1) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số lượng phải >= 1 khi không dùng giỏ hàng");
            }
            quantity = request.getQuantity();
        }
        
        // Validate tour bookable
        if (tour.getStatus() != TourStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour hiện không khả dụng để thanh toán");
        }
        if (tour.getStartDate().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tour đã khởi hành");
        }

        // Validate quantity
        if (quantity > tour.getAvailableSlots()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Số lượng vượt quá số chỗ còn lại (" + tour.getAvailableSlots() + ")");
        }

        // Calculate tour price
        BigDecimal tourPrice = tour.getPrice();
        BigDecimal tourDiscount = tour.getDiscount() != null ? tour.getDiscount() : BigDecimal.ZERO;
        BigDecimal tourFinalPrice = tourPrice.subtract(tourDiscount);
        BigDecimal tourTotal = tourFinalPrice.multiply(BigDecimal.valueOf(quantity));

        // Process addons
        List<CheckoutPreviewResDTO.AddonInfoDTO> addonInfos = new ArrayList<>();
        BigDecimal addonTotal = BigDecimal.ZERO;

        if (request.getAddons() != null && request.getAddons().getItems() != null && !request.getAddons().getItems().isEmpty()) {
            List<UUID> addonIds = request
                .getAddons()
                .getItems()
                .stream()
                .map(CheckoutPreviewReqDTO.AddonItemDTO::getTourAddonId)
                .collect(Collectors.toList());

            List<TourAddon> addons = tourAddonRepository.findAllById(addonIds);

            // Validate all addons belong to this tour
            for (TourAddon addon : addons) {
                if (!addon.getTourId().equals(tour.getId())) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Add-on " + addon.getName() + " không thuộc tour này");
                }
                if (!Boolean.TRUE.equals(addon.getIsActive())) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Add-on " + addon.getName() + " không còn khả dụng");
                }
            }

            Map<UUID, TourAddon> addonMap = addons.stream().collect(Collectors.toMap(TourAddon::getId, addon -> addon));

            for (CheckoutPreviewReqDTO.AddonItemDTO addonItem : request.getAddons().getItems()) {
                TourAddon addon = addonMap.get(addonItem.getTourAddonId());
                if (addon == null) {
                    throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Add-on không tồn tại");
                }

                BigDecimal addonItemTotal = addon.getPrice().multiply(BigDecimal.valueOf(addonItem.getQuantity()));
                addonTotal = addonTotal.add(addonItemTotal);

                CheckoutPreviewResDTO.AddonInfoDTO addonInfo = cartMapper.toCheckoutAddonInfoDto(addon);
                addonInfo.setQuantity(addonItem.getQuantity());
                addonInfo.setTotal(addonItemTotal);
                addonInfos.add(addonInfo);
            }
        }

        // Calculate total
        BigDecimal totalAmount = tourTotal.add(addonTotal);

        return CheckoutPreviewResDTO
            .builder()
            .tour(cartMapper.toCheckoutTourInfoDto(tour))
            .quantity(quantity)
            .price(tourPrice)
            .discount(tourDiscount)
            .addons(addonInfos)
            .totalAmount(totalAmount)
            .build();
    }
}
