package haui.foxtrip.tour.domain;

import haui.foxtrip.domain.AbstractAuditingEntity;
import haui.foxtrip.id.UuidVersion7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tour_itineraries")
public class TourItinerary extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Generator.class)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "activity", nullable = false, columnDefinition = "text")
    private String activity;

    @Override
    public UUID getId() {
        return id;
    }
}
