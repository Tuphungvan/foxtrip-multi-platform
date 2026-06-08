package haui.foxtrip.review.service;

import haui.foxtrip.review.service.dto.DailyBookingDTO;
import haui.foxtrip.review.service.dto.TourOccupancyDTO;

import java.util.List;

public interface AdminStatisticService {
    List<TourOccupancyDTO> getTourOccupancyReport(Integer month, Integer year);
    List<DailyBookingDTO> getDailyBookingReport(String date);
    long countFeaturedLocations(Integer month, Integer year);
}
