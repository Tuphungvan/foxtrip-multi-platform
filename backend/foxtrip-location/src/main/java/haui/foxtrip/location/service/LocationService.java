package haui.foxtrip.location.service;

import haui.foxtrip.location.service.dto.response.LocationResponse;

import java.util.List;
import java.util.UUID;

public interface LocationService {

    LocationResponse getLocationDetail(UUID locationId);
}
