package haui.foxtrip.review.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.order.repository.OrderItemRepository;
import haui.foxtrip.review.domain.Review;
import haui.foxtrip.review.repository.ReviewRepository;
import haui.foxtrip.review.service.ReviewService;
import haui.foxtrip.review.service.dto.CreateReviewReqDTO;
import haui.foxtrip.review.service.dto.ReviewResDTO;
import haui.foxtrip.review.service.dto.TourReviewDTO;
import haui.foxtrip.review.service.dto.UpdateReviewReqDTO;
import haui.foxtrip.review.service.mapper.ReviewMapper;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.repository.TourRepository;
import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public PageData<TourReviewDTO> getTourReviews(UUID tourId, Pageable pageable) {
        // Validate tour exists
        tourRepository.findByIdAndDeletedAtIsNull(tourId).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không tồn tại"));

        Page<Review> reviewPage = reviewRepository.findByTourIdOrderByCreatedAtDesc(tourId, pageable);

        if (reviewPage.isEmpty()) {
            return PageData.of(reviewPage.map(review -> null));
        }

        // Get user IDs
        List<UUID> userIds = reviewPage.getContent().stream().map(Review::getUserId).distinct().collect(Collectors.toList());

        // Query users
        List<User> users = userRepository.findAllById(userIds);
        Map<UUID, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

        // Map to DTO
        Page<TourReviewDTO> dtoPage = reviewPage.map(review -> {
            User user = userMap.get(review.getUserId());
            return reviewMapper.toTourReviewDTO(review, user);
        });

        return PageData.of(dtoPage);
    }

    @Override
    @Transactional
    public ReviewResDTO createReview(UUID userId, CreateReviewReqDTO request) {
        // Validate tour exists
        Tour tour = tourRepository
            .findByIdAndDeletedAtIsNull(request.getTourId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Tour không tồn tại"));

        // Check if user already reviewed this tour
        if (reviewRepository.existsByUserIdAndTourId(userId, request.getTourId())) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), "Bạn đã đánh giá tour này rồi");
        }

        // Check if user has a COMPLETED order for this tour (Verified Buyer)
        if (!orderItemRepository.existsByUserIdAndTourIdAndStatusCompleted(userId, request.getTourId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Chỉ những khách hàng đã hoàn thành tour mới có thể để lại đánh giá");
        }

        // Create review
        Review review = new Review();
        review.setUserId(userId);
        review.setTourId(request.getTourId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());

        review = reviewRepository.save(review);

        // Update tour statistics
        updateTourReviewStats(tour.getId());

        return mapToResDTO(review);
    }

    @Override
    @Transactional
    public ReviewResDTO updateReview(UUID userId, UUID reviewId, UpdateReviewReqDTO request) {
        throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Đánh giá đã gửi không thể chỉnh sửa");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkReviewExists(UUID userId, UUID tourId) {
        return reviewRepository.existsByUserIdAndTourId(userId, tourId);
    }

    private void updateTourReviewStats(UUID tourId) {
        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) {
            return;
        }

        Long reviewCount = reviewRepository.countByTourId(tourId);
        Double avgRating = reviewRepository.calculateAverageRating(tourId);

        tour.setReviewCount(reviewCount.intValue());
        tour.setAverageRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : null);

        tourRepository.save(tour);
    }

    private ReviewResDTO mapToResDTO(Review review) {
        return reviewMapper.toReviewResDTO(review);
    }
}
