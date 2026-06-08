package haui.foxtrip.location.repository;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.location.domain.Location;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID>, JpaSpecificationExecutor<Location> {

    Optional<Location> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Location> findByIdAndDeletedAtIsNotNull(UUID id);

    Optional<Location> findByNameAndProvinceAndAddress(String name, Province province, String address);

    @Query("SELECT COUNT(l) FROM Location l " +
            "WHERE l.deletedAt IS NULL AND l.featuredStartAt < :until AND l.featuredEndAt > :since")
    long countFeaturedInPeriod(@Param("since") Instant since,
                               @Param("until") Instant until);

    @Query("SELECT l FROM Location l " +
            "WHERE FUNCTION('MONTH', l.featuredStartAt) = :month " +
            "AND FUNCTION('YEAR', l.featuredStartAt) = :year " +
            "AND l.priority > 0")
    java.util.List<Location> findFeaturedByStartMonthAndYear(@Param("month") int month, @Param("year") int year);
}
