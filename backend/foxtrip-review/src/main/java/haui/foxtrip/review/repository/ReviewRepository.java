package haui.foxtrip.review.repository;

import haui.foxtrip.review.domain.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByTourIdOrderByCreatedAtDesc(UUID tourId, Pageable pageable);

    Optional<Review> findByUserIdAndTourId(UUID userId, UUID tourId);

    boolean existsByUserIdAndTourId(UUID userId, UUID tourId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tourId = :tourId")
    Double calculateAverageRating(@Param("tourId") UUID tourId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tourId = :tourId")
    Long countByTourId(@Param("tourId") UUID tourId);
}
