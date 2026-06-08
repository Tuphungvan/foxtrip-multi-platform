package haui.foxtrip.tour.repository;

import haui.foxtrip.tour.domain.TourItinerary;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourItineraryRepository extends JpaRepository<TourItinerary, UUID> {
    List<TourItinerary> findByTourIdOrderByDayNumberAscPositionAsc(UUID tourId);
    List<TourItinerary> findByTourIdIn(List<UUID> tourIds);
    void deleteByTourId(UUID tourId);
}
