package haui.foxtrip.tour.service.mapper;

import haui.foxtrip.location.domain.Location;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.domain.TourItinerary;
import haui.foxtrip.tour.service.dto.request.RestartTourReqDTO;
import haui.foxtrip.tour.service.dto.request.TourAddonReqDTO;
import haui.foxtrip.tour.service.dto.request.TourAdminReqDTO;
import haui.foxtrip.tour.service.dto.request.TourItineraryReqDTO;
import haui.foxtrip.tour.service.dto.request.UpdateTourReqDTO;
import haui.foxtrip.tour.service.dto.response.ItineraryItemResDTO;
import haui.foxtrip.tour.service.dto.response.TourAddonResDTO;
import haui.foxtrip.tour.service.dto.response.TourCreationResDTO;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourListResDTO;
import haui.foxtrip.tour.service.dto.response.TourCardResponse;
import haui.foxtrip.tour.service.dto.response.MarkerResponse;
import haui.foxtrip.tour.service.dto.response.GuideResDTO;
import haui.foxtrip.tour.service.dto.response.TourVideoCardResponse;
import haui.foxtrip.user.domain.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TourMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTourFromDTO(UpdateTourReqDTO dto, @MappingTarget Tour entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTourFromRestartDTO(RestartTourReqDTO dto, @MappingTarget Tour entity);

    Tour toTourEntity(TourAdminReqDTO dto);
    TourCreationResDTO toCreationDto(Tour entity);
    @Mapping(target = "finalPrice", expression = "java(calculateFinalPrice(entity.getPrice(), entity.getDiscount()))")
    TourListResDTO toListDto(Tour entity);
    @Mapping(target = "finalPrice", expression = "java(calculateFinalPrice(entity.getPrice(), entity.getDiscount()))")
    TourCardResponse toCardResponse(Tour entity);
    GuideResDTO toGuideResDto(User user);
    @Mapping(target = "title", source = "name")
    MarkerResponse toMapMarkerDto(Tour tour);

    @Mapping(target = "title", source = "name")
    TourVideoCardResponse toVideoCardResponse(Tour tour);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "lat", expression = "java(location.getCoordinates() != null ? location.getCoordinates().getY() : null)")
    @Mapping(target = "lng", expression = "java(location.getCoordinates() != null ? location.getCoordinates().getX() : null)")
    @Mapping(target = "type", expression = "java(location.getType() != null ? \"AD_\" + location.getType().name() : \"AD\")")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "address", source = "address")
    MarkerResponse toAdMarkerDto(Location location);

    default MarkerResponse toMapMarkerDto(Tour tour, Location location) {
        if (tour == null) return null;
        MarkerResponse dto = toMapMarkerDto(tour);
        dto.setType("TOUR");
        if (location != null) {
            dto.setAddress(location.getAddress());
            if (location.getCoordinates() != null) {
                dto.setLat(location.getCoordinates().getY());
                dto.setLng(location.getCoordinates().getX());
            }
        }
        return dto;
    }

    @Mapping(target = "finalPrice", expression = "java(calculateFinalPrice(entity.getPrice(), entity.getDiscount()))")
    TourDetailResDTO toDetailDto(Tour entity);

    default BigDecimal calculateFinalPrice(BigDecimal price, BigDecimal discount) {
        if (price == null) return BigDecimal.ZERO;
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) return price;
        return price.multiply(BigDecimal.valueOf(100).subtract(discount))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    TourItinerary toItineraryEntity(TourItineraryReqDTO.ItineraryItemReq dto);
    ItineraryItemResDTO toItineraryItemDto(TourItinerary entity);
    TourAddon toAddonEntity(TourAddonReqDTO.AddonItemReq dto);
    TourAddonResDTO toAddonDto(TourAddon entity);
}
