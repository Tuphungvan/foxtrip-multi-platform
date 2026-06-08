package haui.foxtrip.tour.web.rest;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.enums.Province;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourCardResponse;
import haui.foxtrip.tour.service.dto.response.MarkerResponse;
import haui.foxtrip.tour.service.dto.response.TourVideoCardResponse;
import haui.foxtrip.tour.domain.enums.TourCategory;
import haui.foxtrip.tour.service.TourService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping("/search")
    public ApiResponse<PageData<TourCardResponse>> searchTours(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "province", required = false) Province province,
            @RequestParam(value = "category", required = false) TourCategory category,
            @RequestParam(value = "priceFrom", required = false) BigDecimal priceFrom,
            @RequestParam(value = "priceTo", required = false) BigDecimal priceTo,
            @RequestParam(value = "startDate", required = false) Instant startDate,
            @RequestParam(value = "endDate", required = false) Instant endDate,
            Pageable pageable) {
        return ApiResponse.success("Lấy danh sách tour thành công",
                tourService.searchTours(keyword, province, category, priceFrom, priceTo, startDate, endDate, pageable));
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<TourCardResponse>> getUpcomingTours(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        return ApiResponse.success("Lấy danh sách tour sắp tới thành công",
                tourService.getUpcomingTours(limit));
    }

    @GetMapping("/discounted")
    public ApiResponse<List<TourCardResponse>> getDiscountedTours(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        return ApiResponse.success("Lấy danh sách tour giảm giá thành công",
                tourService.getDiscountedTours(limit));
    }

    @GetMapping("/discovery-map")
    public ApiResponse<List<MarkerResponse>> getDiscoveryMap() {
        return ApiResponse.success("Lấy danh sách bản đồ khám phá thành công", tourService.getDiscoveryMap());
    }

    @GetMapping("/short-videos")
    public ApiResponse<List<TourVideoCardResponse>> getTourVideoCards() {
        return ApiResponse.success("Lấy danh sách video ngắn thành công", tourService.getTourVideoCards());
    }

    @GetMapping("/{slug}")
    public ApiResponse<TourDetailResDTO> getTourDetail(@PathVariable("slug") String slug) {
        return ApiResponse.success("Lấy chi tiết tour thành công", tourService.getTourDetail(slug));
    }
}
