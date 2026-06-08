package haui.foxtrip.location.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.location.service.dto.response.LocationResponse;

import haui.foxtrip.location.service.LocationService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/{locationId}")
    public ApiResponse<LocationResponse> getLocationDetail(@PathVariable("locationId") UUID locationId) {
        return ApiResponse.success("Lấy chi tiết location thành công",
                locationService.getLocationDetail(locationId));
    }
}
