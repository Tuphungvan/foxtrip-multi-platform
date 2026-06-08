package haui.foxtrip.order.repository;

import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.domain.enums.OrderStatus;
import haui.foxtrip.order.service.dto.PassengerResDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    Optional<Order> findByOrderCode(String orderCode);
    
    boolean existsByOrderCode(String orderCode);
    
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId " +
           "AND (cast(:status as string) IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatus(@Param("userId") UUID userId, 
                                       @Param("status") OrderStatus status, 
                                       Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(cast(:status as string) IS NULL OR o.status = :status) " +
           "AND (cast(:customerEmail as string) IS NULL OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', cast(:customerEmail as string), '%'))) " +
           "AND (cast(:orderCode as string) IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', cast(:orderCode as string), '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByAdminFilters(@Param("status") OrderStatus status,
                                    @Param("customerEmail") String customerEmail,
                                    @Param("orderCode") String orderCode,
                                    Pageable pageable);
    
    List<Order> findByStatusAndExpiresAtBefore(@Param("status") OrderStatus status, @Param("expiresAt") Instant expiresAt);

    @Modifying
    @Query(value = "UPDATE orders SET status = :status WHERE id IN " +
            "(SELECT order_id FROM order_items WHERE tour_id = :tourId) " +
            "AND status = 'PAID'", nativeQuery = true)
    void updateStatusByTourId(@Param("tourId") UUID tourId, @Param("status") String status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.status = haui.foxtrip.order.domain.enums.OrderStatus.COMPLETED " +
            "AND o.paidAt >= :since AND o.paidAt < :until")
    BigDecimal calculateTotalRevenueInPeriod(@Param("since") Instant since, @Param("until") Instant until);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.status = haui.foxtrip.order.domain.enums.OrderStatus.COMPLETED " +
            "AND o.paidAt >= :since AND o.paidAt < :until")
    long countCompletedOrdersInPeriod(@Param("since") Instant since, @Param("until") Instant until);

    @Query("SELECT SUM(oi.finalPrice) FROM OrderItem oi " +
            "JOIN Order o ON oi.orderId = o.id " +
            "WHERE o.status = haui.foxtrip.order.domain.enums.OrderStatus.COMPLETED " +
            "AND o.paidAt >= :since AND o.paidAt < :until")
    BigDecimal calculateTourRevenueInPeriod(@Param("since") Instant since, @Param("until") Instant until);

    @Query("SELECT SUM(oa.priceAtTime * oa.quantity) FROM OrderAddon oa " +
            "JOIN Order o ON oa.orderId = o.id " +
            "WHERE o.status = haui.foxtrip.order.domain.enums.OrderStatus.COMPLETED " +
            "AND o.paidAt >= :since AND o.paidAt < :until")
    BigDecimal calculateAddonRevenueInPeriod(@Param("since") Instant since, @Param("until") Instant until);

    @Query("SELECT o FROM Order o WHERE o.id IN " +
            "(SELECT oi.orderId FROM OrderItem oi WHERE oi.tourId = :tourId) " +
            "AND o.status = :status")
    List<Order> findByTourIdAndStatus(@Param("tourId") UUID tourId, @Param("status") OrderStatus status);

    @Query("SELECT new haui.foxtrip.order.service.dto.PassengerResDTO(o.orderCode, o.customerName, o.customerPhone, o.customerEmail, oi.quantity, o.status) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE oi.tourId = :tourId AND o.status = 'PAID'")
    List<PassengerResDTO> findPassengersByTourId(@Param("tourId") UUID tourId);

    @Query("SELECT t.id, t.name, t.startDate, COALESCE(SUM(oi.quantity), 0) " +
           "FROM Tour t " +
           "LEFT JOIN OrderItem oi ON oi.tourId = t.id " +
           "LEFT JOIN Order o ON oi.orderId = o.id AND (o.status = haui.foxtrip.order.domain.enums.OrderStatus.PAID OR o.status = haui.foxtrip.order.domain.enums.OrderStatus.COMPLETED) " +
           "WHERE t.status = haui.foxtrip.tour.domain.enums.TourStatus.COMPLETED " +
           "AND t.startDate >= :since AND t.startDate < :until " +
           "GROUP BY t.id, t.name, t.startDate")
    List<Object[]> findTourOccupancyRaw(@Param("since") Instant since, @Param("until") Instant until);

    @Query("SELECT oi.tourId, oi.tourNameAtTime, COUNT(DISTINCT o.id), SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.createdAt >= :start AND o.createdAt <= :end " +
           "AND o.status = haui.foxtrip.order.domain.enums.OrderStatus.PAID " +
           "GROUP BY oi.tourId, oi.tourNameAtTime " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findDailyBookingsRaw(@Param("start") Instant start, @Param("end") Instant end);
}
