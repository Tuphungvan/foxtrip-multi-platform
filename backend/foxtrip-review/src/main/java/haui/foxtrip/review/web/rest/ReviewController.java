package haui.foxtrip.review.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.review.service.ReviewService;
import haui.foxtrip.review.service.dto.CreateReviewReqDTO;
import haui.foxtrip.review.service.dto.ReviewResDTO;
import haui.foxtrip.review.service.dto.TourReviewDTO;
import haui.foxtrip.review.service.dto.UpdateReviewReqDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/tours/{tourId}/reviews")
    public ResponseEntity<ApiResponse<PageData<TourReviewDTO>>> getTourReviews(
            @PathVariable("tourId") UUID tourId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageData<TourReviewDTO> reviews = reviewService.getTourReviews(tourId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách review thành công", reviews));
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<ReviewResDTO>> createReview(@Valid @RequestBody CreateReviewReqDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        ReviewResDTO review = reviewService.createReview(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo review thành công", review));
    }

    @PatchMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<ReviewResDTO>> updateReview(
            @PathVariable("reviewId") UUID reviewId,
            @Valid @RequestBody UpdateReviewReqDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        ReviewResDTO review = reviewService.updateReview(userId, reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật review thành công", review));
    }

    @GetMapping("/reviews/check/{tourId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkReviewExists(@PathVariable("tourId") UUID tourId) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Unauthorized"));
        boolean exists = reviewService.checkReviewExists(userId, tourId);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra review thành công", exists));
    }
}
