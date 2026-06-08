package haui.foxtrip.web.rest;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.order.service.OrderService;
import haui.foxtrip.order.service.dto.PassengerResDTO;
import haui.foxtrip.tour.service.AdminTourService;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourListResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guide/tours")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('GUIDE')")
public class GuideTourController {

    private final AdminTourService adminTourService;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageData<TourListResDTO>>> getMyTours(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        UUID guideId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Chưa đăng nhập"));
        
        // Sắp xếp theo ngày bắt đầu như yêu cầu (tăng dần để thấy cái gần nhất)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate"));
        PageData<TourListResDTO> tours = adminTourService.getToursByGuide(guideId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tour thành công", tours));
    }

    @GetMapping("/{tourId}")
    public ResponseEntity<ApiResponse<TourDetailResDTO>> getTourDetail(@PathVariable("tourId") UUID tourId) {
        // Có thể thêm check xem tour này có phải của guide này không nếu muốn bảo mật kỹ hơn
        TourDetailResDTO tour = adminTourService.getTour(tourId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết tour thành công", tour));
    }

    @GetMapping("/{tourId}/passengers")
    public ResponseEntity<ApiResponse<List<PassengerResDTO>>> getPassengers(@PathVariable("tourId") UUID tourId) {
        List<PassengerResDTO> passengers = orderService.getPassengersByTour(tourId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hành khách thành công", passengers));
    }

    @PostMapping("/{tourId}/check-in")
    public ResponseEntity<ApiResponse<Void>> checkIn(
            @PathVariable("tourId") UUID tourId,
            @RequestParam("orderCode") String orderCode) {
        orderService.checkIn(tourId, orderCode);
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công"));
    }
}
