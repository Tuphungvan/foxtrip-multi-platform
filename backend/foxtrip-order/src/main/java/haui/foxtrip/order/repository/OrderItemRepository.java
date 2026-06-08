package haui.foxtrip.order.repository;

import haui.foxtrip.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    
    List<OrderItem> findByOrderId(UUID orderId);
    
    void deleteByOrderId(UUID orderId);

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "JOIN Order o ON oi.orderId = o.id " +
            "WHERE o.userId = :userId AND oi.tourId = :tourId AND o.status = 'COMPLETED'")
    boolean existsByUserIdAndTourIdAndStatusCompleted(
            @Param("userId") UUID userId,
            @Param("tourId") UUID tourId);

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "JOIN Order o ON oi.orderId = o.id " +
            "WHERE o.userId = :userId AND oi.tourId = :tourId AND (o.status = 'PAID' OR o.status = 'COMPLETED')")
    boolean existsByUserIdAndTourIdAndStatusPaidOrCompleted(
            @Param("userId") UUID userId,
            @Param("tourId") UUID tourId);

    boolean existsByOrderIdAndTourId(UUID orderId, UUID tourId);

    List<OrderItem> findAllByOrderIdIn(List<UUID> orderIds);
}
