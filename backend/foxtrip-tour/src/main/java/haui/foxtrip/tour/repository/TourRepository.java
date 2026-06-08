package haui.foxtrip.tour.repository;

import haui.foxtrip.tour.domain.Tour;

import java.util.Optional;
import java.util.UUID;

import haui.foxtrip.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.Instant;

@Repository
public interface TourRepository extends JpaRepository<Tour, UUID>, JpaSpecificationExecutor<Tour> {

        Optional<Tour> findBySlugAndDeletedAtIsNull(String slug);

        Optional<Tour> findByIdAndDeletedAtIsNull(UUID id);

        boolean existsBySlugAndDeletedAtIsNull(String slug);

        boolean existsBySlug(String slug);

        @Query("""
                        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
                        FROM Tour t
                        WHERE t.deletedAt IS NULL
                        AND t.status IN ('ACTIVE','ONGOING')
                        AND t.guideId = :guideId
                        AND t.startDate <= :endDate
                        AND t.endDate >= :startDate
                        """)
        boolean existsOverlappingTour(
                        @Param("guideId") UUID guideId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("""
                        SELECT u
                        FROM User u
                        WHERE u.role = 'GUIDE'
                        AND u.deletedAt IS NULL
                        AND u.id NOT IN (
                            SELECT t.guideId
                            FROM Tour t
                            WHERE t.deletedAt IS NULL
                            AND t.status IN ('ACTIVE','ONGOING')
                            AND t.guideId IS NOT NULL
                            AND t.startDate <= :endDate
                            AND t.endDate >= :startDate
                        )
                        """)
        List<User> findAvailableGuides(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

        @Modifying
        @Query("UPDATE Tour t SET t.availableSlots = t.availableSlots - :quantity "
                        + "WHERE t.id = :tourId AND t.availableSlots >= :quantity")
        int decrementSlots(@Param("tourId") UUID tourId, @Param("quantity") int quantity);

        @Modifying
        @Query("UPDATE Tour t SET t.availableSlots = t.availableSlots + :quantity " + "WHERE t.id = :tourId")
        int incrementSlots(@Param("tourId") UUID tourId, @Param("quantity") int quantity);

        @Query(value = "SELECT * FROM tours t WHERE t.deleted_at IS NULL "
                        + "AND t.status = 'ACTIVE' AND t.available_slots > 0 "
                        + "AND t.start_date > NOW() "
                        + "AND (t.discount IS NULL OR t.discount = 0) "
                        + "ORDER BY t.start_date ASC LIMIT :limit", nativeQuery = true)
        List<Tour> findUpcomingNonDiscountedTours(@Param("limit") int limit);

        @Query(value = "SELECT * FROM tours t WHERE t.deleted_at IS NULL "
                        + "AND t.status = 'ACTIVE' AND t.available_slots > 0 "
                        + "AND t.start_date > NOW() "
                        + "AND t.discount > 0 "
                        + "ORDER BY t.start_date ASC LIMIT :limit", nativeQuery = true)
        List<Tour> findUpcomingDiscountedTours(@Param("limit") int limit);

        @Query(value = "SELECT COUNT(*) > 0 FROM orders o "
                        + "JOIN order_items oi ON o.id = oi.order_id "
                        + "WHERE o.user_id = :userId AND oi.tour_id = :tourId "
                        + "AND (o.status = 'PAID' OR o.status = 'COMPLETED')", nativeQuery = true)
        boolean existsPaidOrderForUserAndTour(@Param("userId") UUID userId, @Param("tourId") UUID tourId);

        @Query("SELECT t FROM Tour t WHERE t.deletedAt IS NULL AND t.status = 'ACTIVE' "
                        + "AND t.availableSlots > 0 AND t.startDate > CURRENT_TIMESTAMP "
                        + "AND t.shortId IS NOT NULL AND t.shortId <> ''")
        List<Tour> findAllActiveWithShortId();
}
