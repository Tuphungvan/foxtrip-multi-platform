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
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Generator.class)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "tour_name_at_time", nullable = false)
    private String tourNameAtTime;

    @Column(name = "start_date_at_time", nullable = false)
    private Instant startDateAtTime;

    @Column(name = "end_date_at_time", nullable = false)
    private Instant endDateAtTime;

    @Column(name = "thumbnail_at_time", nullable = false)
    private String thumbnailAtTime;

    @Column(name = "price_at_time", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAtTime;

    @Column(name = "discount_at_time", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAtTime = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;

    @Override
    public UUID getId() {
        return id;
    }
}
