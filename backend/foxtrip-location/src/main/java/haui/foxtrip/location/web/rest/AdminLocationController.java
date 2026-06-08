package haui.foxtrip.location.web.rest;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.location.service.dto.request.CreateLocationRequest;
import haui.foxtrip.location.service.dto.request.UpdateLocationRequest;
import haui.foxtrip.location.service.dto.response.LocationResponse;
import haui.foxtrip.location.service.AdminLocationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/locations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
@SuppressWarnings("unused")
public class AdminLocationController {

    private final AdminLocationService adminLocationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LocationResponse>> createLocation(@Valid @RequestBody CreateLocationRequest requestDTO) {
        LocationResponse created = adminLocationService.createLocation(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo địa điểm thành công", created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateLocationRequest requestDTO) {
        LocationResponse updated = adminLocationService.updateLocation(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa điểm thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable("id") UUID id) {
        adminLocationService.deleteLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa điểm thành công"));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreLocation(@PathVariable("id") UUID id) {
        adminLocationService.restoreLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục địa điểm thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageData<LocationResponse>>> getLocations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageData<LocationResponse> data = adminLocationService.getLocations(keyword, type, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách địa điểm thành công", data)
        );
    }
}
