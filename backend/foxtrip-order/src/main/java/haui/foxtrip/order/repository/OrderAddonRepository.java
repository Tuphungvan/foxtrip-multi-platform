package haui.foxtrip.order.repository;

import haui.foxtrip.order.domain.OrderAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderAddonRepository extends JpaRepository<OrderAddon, UUID> {
    
    List<OrderAddon> findByOrderId(UUID orderId);
    
    void deleteByOrderId(UUID orderId);

    List<OrderAddon> findAllByOrderIdIn(List<UUID> orderIds);
}
