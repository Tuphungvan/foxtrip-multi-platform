package haui.foxtrip.user.domain;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.domain.AbstractAuditingEntity;
import haui.foxtrip.id.UuidVersion7Generator;
import haui.foxtrip.user.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Generator.class)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 50)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "phone_number", length = 10, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "province")
    private Province province;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl = "https://res.cloudinary.com/do1ill8ba/image/upload/v1775034651/default_image.png";

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Override
    public UUID getId() {
        return id;
    }
}
