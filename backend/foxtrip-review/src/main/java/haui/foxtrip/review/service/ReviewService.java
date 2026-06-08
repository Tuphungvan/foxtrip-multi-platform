package haui.foxtrip.review.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.review.service.dto.CreateReviewReqDTO;
import haui.foxtrip.review.service.dto.ReviewResDTO;
import haui.foxtrip.review.service.dto.TourReviewDTO;
import haui.foxtrip.review.service.dto.UpdateReviewReqDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    PageData<TourReviewDTO> getTourReviews(UUID tourId, Pageable pageable);

    ReviewResDTO createReview(UUID userId, CreateReviewReqDTO request);

    ReviewResDTO updateReview(UUID userId, UUID reviewId, UpdateReviewReqDTO request);

    boolean checkReviewExists(UUID userId, UUID tourId);
}
