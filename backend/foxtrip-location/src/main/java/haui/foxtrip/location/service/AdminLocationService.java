package haui.foxtrip.location.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.location.service.dto.request.CreateLocationRequest;
import haui.foxtrip.location.service.dto.request.UpdateLocationRequest;
import haui.foxtrip.location.service.dto.response.LocationResponse;

import java.util.UUID;

public interface AdminLocationService {

    LocationResponse createLocation(CreateLocationRequest dto);

    LocationResponse updateLocation(UUID id, UpdateLocationRequest dto);

    void deleteLocation(UUID id);

    void restoreLocation(UUID id);

    PageData<LocationResponse> getLocations(String keyword, String type, int page, int size);
}