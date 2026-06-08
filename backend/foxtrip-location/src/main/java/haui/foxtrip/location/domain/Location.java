package haui.foxtrip.location.domain;

import haui.foxtrip.domain.AbstractAuditingEntity;
import haui.foxtrip.id.UuidVersion7Generator;
import haui.foxtrip.location.domain.enums.LocType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import haui.foxtrip.common.enums.Province;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.locationtech.jts.geom.Point;

@Data
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class Location extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Generator.class)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "province", nullable = false)
    private Province province;

    @Column(name = "address", nullable = false, columnDefinition = "text")
    private String address;

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinates;

    @Column(name = "mapbox_place_id", unique = true)
    private String mapboxPlaceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LocType type = LocType.OTHER;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "featured_start_at")
    private Instant featuredStartAt;

    @Column(name = "featured_end_at")
    private Instant featuredEndAt;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Override
    public UUID getId() {
        return id;
    }
}
