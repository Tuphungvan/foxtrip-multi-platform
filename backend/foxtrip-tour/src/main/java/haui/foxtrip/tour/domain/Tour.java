package haui.foxtrip.tour.domain;

import haui.foxtrip.domain.AbstractAuditingEntity;
import haui.foxtrip.id.UuidVersion7Generator;
import haui.foxtrip.tour.domain.enums.TourCategory;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.domain.enums.TourSetupStep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import haui.foxtrip.common.enums.Province;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tours")
public class Tour extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Generator.class)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "province", nullable = false)
    private Province province;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TourCategory category;

    @Column(name = "short_id", nullable = false, unique = true)
    private String shortId;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "discount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "slots", nullable = false)
    private Integer slots;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Column(name = "guide_id")
    private UUID guideId;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TourStatus status = TourStatus.HIDDEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "setup_step", nullable = false)
    private TourSetupStep setupStep = TourSetupStep.BASIC_DONE;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Override
    public UUID getId() {
        return id;
    }
}
