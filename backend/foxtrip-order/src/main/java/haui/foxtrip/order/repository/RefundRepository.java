package haui.foxtrip.order.repository;

import haui.foxtrip.order.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    
    Optional<Refund> findByOrderId(UUID orderId);
    
    Optional<Refund> findTopByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
