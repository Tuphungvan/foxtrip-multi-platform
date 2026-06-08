package haui.foxtrip.tour.repository;

import haui.foxtrip.tour.domain.TourAddon;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourAddonRepository extends JpaRepository<TourAddon, UUID> {
    List<TourAddon> findByTourId(UUID tourId);
    List<TourAddon> findByTourIdAndIsActiveTrue(UUID tourId);
    List<TourAddon> findByTourIdIn(List<UUID> tourIds);
    void deleteByTourId(UUID tourId);
}
