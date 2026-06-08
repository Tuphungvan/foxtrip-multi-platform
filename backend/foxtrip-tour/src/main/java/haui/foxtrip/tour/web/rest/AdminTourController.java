package haui.foxtrip.tour.web.rest;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.service.dto.request.TourAddonReqDTO;
import haui.foxtrip.tour.service.dto.request.TourAdminReqDTO;
import haui.foxtrip.tour.service.dto.request.TourItineraryReqDTO;
import haui.foxtrip.tour.service.dto.request.UpdateTourReqDTO;
import haui.foxtrip.tour.service.dto.request.RestartTourReqDTO;
import haui.foxtrip.tour.service.dto.response.GuideResDTO;
import haui.foxtrip.tour.service.dto.response.TourCreationResDTO;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourListResDTO;
import haui.foxtrip.tour.service.AdminTourService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tours")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
public class AdminTourController {

    private final AdminTourService adminTourService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageData<TourListResDTO>>> getTours(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) TourStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        PageData<TourListResDTO> tours = adminTourService.getTours(keyword, type, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tour thành công", tours));
    }

    @GetMapping("/{tourId}")
    public ResponseEntity<ApiResponse<TourDetailResDTO>> getTour(@PathVariable("tourId") UUID tourId) {
        TourDetailResDTO tour = adminTourService.getTour(tourId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tour thành công", tour));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TourCreationResDTO>> createTour(@Valid @RequestBody TourAdminReqDTO requestDTO) {
        TourCreationResDTO created = adminTourService.createTour(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tour thành công", created));
    }

    @PatchMapping("/{tourId}")
    public ResponseEntity<ApiResponse<TourListResDTO>> updateTour(
            @PathVariable("tourId") UUID tourId,
            @Valid @RequestBody UpdateTourReqDTO requestDTO) {
        TourListResDTO updated = adminTourService.updateTour(tourId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tour thành công", updated));
    }

    @PutMapping("/{tourId}/itineraries")
    public ResponseEntity<ApiResponse<TourDetailResDTO>> upsertItineraries(
            @PathVariable("tourId") UUID tourId,
            @Valid @RequestBody TourItineraryReqDTO requestDTO) {
        TourDetailResDTO updated = adminTourService.upsertItineraries(tourId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch trình thành công", updated));
    }

    @PutMapping("/{tourId}/addons")
    public ResponseEntity<ApiResponse<TourDetailResDTO>> upsertAddons(
            @PathVariable("tourId") UUID tourId,
            @Valid @RequestBody TourAddonReqDTO requestDTO) {
        TourDetailResDTO updated = adminTourService.upsertAddons(tourId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật dịch vụ đi kèm thành công", updated));
    }

    @GetMapping("/{tourId}/guide-suggestions")
    public ResponseEntity<ApiResponse<List<GuideResDTO>>> getGuideSuggestions(
            @PathVariable("tourId") UUID tourId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        List<GuideResDTO> suggestions = adminTourService.getGuideSuggestions(tourId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hướng dẫn viên gợi ý thành công", suggestions));
    }

    @PatchMapping("/{tourId}/guide")
    public ResponseEntity<ApiResponse<TourDetailResDTO>> assignGuide(
            @PathVariable("tourId") UUID tourId,
            @RequestParam("guideId") UUID guideId) {
        TourDetailResDTO updated = adminTourService.assignGuide(tourId, guideId);
        return ResponseEntity.ok(ApiResponse.success("Phân công hướng dẫn viên thành công", updated));
    }

    @PatchMapping("/{tourId}/restart")
    public ResponseEntity<ApiResponse<TourDetailResDTO>> restartTour(
            @PathVariable("tourId") UUID tourId,
            @Valid @RequestBody RestartTourReqDTO requestDTO) {
        TourDetailResDTO updated = adminTourService.restartTour(tourId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Khởi tạo lại tour thành công", updated));
    }

    @DeleteMapping("/{tourId}")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable("tourId") UUID tourId) {
        adminTourService.deleteTour(tourId);
        return ResponseEntity.ok(ApiResponse.success("Xóa tour thành công", null));
    }

    @PatchMapping("/{tourId}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreTour(@PathVariable("tourId") UUID tourId) {
        adminTourService.restoreTour(tourId);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục tour thành công", null));
    }
}
