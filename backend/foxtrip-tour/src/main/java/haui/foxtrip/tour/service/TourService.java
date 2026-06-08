package haui.foxtrip.tour.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.enums.Province;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourCardResponse;
import haui.foxtrip.tour.service.dto.response.MarkerResponse;
import haui.foxtrip.tour.service.dto.response.TourVideoCardResponse;
import haui.foxtrip.tour.domain.enums.TourCategory;
import java.math.BigDecimal;
import java.util.List;
import java.time.Instant;

import org.springframework.data.domain.Pageable;

public interface TourService {

    PageData<TourCardResponse> searchTours(String keyword, Province province, TourCategory category,
            BigDecimal priceFrom, BigDecimal priceTo, Instant startDate, Instant endDate, Pageable pageable);

    TourDetailResDTO getTourDetail(String slug);

    List<TourCardResponse> getUpcomingTours(Integer limit);

    List<TourCardResponse> getDiscountedTours(Integer limit);

    List<MarkerResponse> getDiscoveryMap();

    List<TourVideoCardResponse> getTourVideoCards();
}
