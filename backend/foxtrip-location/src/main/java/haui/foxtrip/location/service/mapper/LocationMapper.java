package haui.foxtrip.location.service.mapper;

import haui.foxtrip.location.service.dto.request.CreateLocationRequest;
import haui.foxtrip.location.service.dto.request.UpdateLocationRequest;
import haui.foxtrip.location.service.dto.response.LocationResponse;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import haui.foxtrip.location.domain.Location;

import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    @Mapping(target = "lat", source = "coordinates", qualifiedByName = "pointToLat")
    @Mapping(target = "lng", source = "coordinates", qualifiedByName = "pointToLng")
    LocationResponse toDto(Location entity);

    @Mapping(target = "coordinates", source = ".", qualifiedByName = "dtoToPoint")
    Location toEntity(CreateLocationRequest dto);

    @Mapping(target = "coordinates", source = ".", qualifiedByName = "dtoToPoint")
    @Mapping(target = "featuredStartAt", ignore = true)
    @Mapping(target = "featuredEndAt", ignore = true)
    @Mapping(target = "priority", ignore = true)
    void updateEntityFromDto(UpdateLocationRequest dto, @MappingTarget Location entity);

    @Named("pointToLat")
    default Double pointToLat(Point point) {
        return point != null ? point.getY() : null;
    }

    @Named("pointToLng")
    default Double pointToLng(Point point) {
        return point != null ? point.getX() : null;
    }

    @Named("dtoToPoint")
    default Point dtoToPoint(Object dto) {
        Double lat = null;
        Double lng = null;
        if (dto instanceof CreateLocationRequest c) {
            lat = c.getLat();
            lng = c.getLng();
        } else if (dto instanceof UpdateLocationRequest u) {
            lat = u.getLat();
            lng = u.getLng();
        }
        if (lat == null || lng == null) return null;
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        return factory.createPoint(new Coordinate(lng, lat));
    }
}
