package haui.foxtrip.tour.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.service.dto.request.TourAddonReqDTO;
import haui.foxtrip.tour.service.dto.request.TourAdminReqDTO;
import haui.foxtrip.tour.service.dto.request.TourItineraryReqDTO;
import haui.foxtrip.tour.service.dto.request.UpdateTourReqDTO;
import haui.foxtrip.tour.service.dto.request.RestartTourReqDTO;
import haui.foxtrip.tour.service.dto.response.GuideResDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import haui.foxtrip.tour.service.dto.response.TourCreationResDTO;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourListResDTO;
import java.util.List;
import java.util.UUID;

public interface AdminTourService {

    TourCreationResDTO createTour(TourAdminReqDTO dto);

    TourListResDTO updateTour(UUID tourId, UpdateTourReqDTO dto);

    TourDetailResDTO upsertItineraries(UUID tourId, TourItineraryReqDTO dto);

    TourDetailResDTO upsertAddons(UUID tourId, TourAddonReqDTO dto);

    List<GuideResDTO> getGuideSuggestions(UUID tourId, String startDate, String endDate);

    TourDetailResDTO assignGuide(UUID tourId, UUID guideId);

    TourDetailResDTO restartTour(UUID tourId, RestartTourReqDTO dto);

    void deleteTour(UUID tourId);

    void restoreTour(UUID tourId);

    TourDetailResDTO getTour(UUID tourId);

    PageData<TourListResDTO> getTours(String keyword, String type, TourStatus status, int page, int size);

    PageData<TourListResDTO> getToursByGuide(UUID guideId, Pageable pageable);
}
