package haui.foxtrip.order.domain;

import haui.foxtrip.domain.AbstractAuditingEntity;
import haui.foxtrip.id.UuidVersion7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@NoArgsConstructor
@Entity
@Table(name = "order_addons")
public class OrderAddon extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Generator.class)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "tour_addon_id", nullable = false)
    private UUID tourAddonId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "price_at_time", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAtTime;

    @Override
    public UUID getId() {
        return id;
    }
}
