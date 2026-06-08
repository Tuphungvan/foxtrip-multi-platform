package haui.foxtrip.cart.service.mapper;

import haui.foxtrip.cart.domain.CartItem;
import haui.foxtrip.cart.service.dto.CartResDTO;
import haui.foxtrip.cart.service.dto.CheckoutPreviewResDTO;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.service.dto.response.TourAddonResDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "tour", ignore = true)
    CartResDTO.CartItemDTO toItemDto(CartItem entity);

    @Mapping(target = "addons", ignore = true)
    CartResDTO.TourInfoDTO toTourInfoDto(Tour entity);

    CheckoutPreviewResDTO.TourInfoDTO toCheckoutTourInfoDto(Tour entity);

    @Mapping(target = "unitPrice", source = "price")
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "total", ignore = true)
    CheckoutPreviewResDTO.AddonInfoDTO toCheckoutAddonInfoDto(TourAddon entity);

    TourAddonResDTO toAddonDto(TourAddon entity);
}
