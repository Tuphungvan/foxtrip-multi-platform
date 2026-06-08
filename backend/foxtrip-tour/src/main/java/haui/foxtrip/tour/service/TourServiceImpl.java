package haui.foxtrip.tour.service;

import haui.foxtrip.common.enums.Province;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.location.domain.Location;
import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.domain.TourItinerary;
import haui.foxtrip.tour.service.dto.response.ItineraryItemResDTO;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourCardResponse;
import haui.foxtrip.tour.service.dto.response.MarkerResponse;
import haui.foxtrip.tour.service.dto.response.TourVideoCardResponse;
import haui.foxtrip.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import haui.foxtrip.tour.domain.enums.TourCategory;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.repository.TourAddonRepository;
import haui.foxtrip.tour.repository.TourItineraryRepository;
import haui.foxtrip.tour.repository.TourRepository;
import haui.foxtrip.tour.service.mapper.TourMapper;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.common.dto.PageData;
import jakarta.persistence.criteria.Predicate;
import org.springframework.cache.annotation.Cacheable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final TourItineraryRepository tourItineraryRepository;
    private final TourAddonRepository tourAddonRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional(readOnly = true)
    public PageData<TourCardResponse> searchTours(String keyword, Province province, TourCategory category,
            BigDecimal priceFrom, BigDecimal priceTo, Instant startDate, Instant endDate, Pageable pageable) {
        Specification<Tour> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), TourStatus.ACTIVE));
            predicates.add(cb.greaterThan(root.get("availableSlots"), 0));
            predicates.add(cb.greaterThan(root.get("startDate"), Instant.now()));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            if (province != null)
                predicates.add(cb.equal(root.get("province"), province));
            if (category != null)
                predicates.add(cb.equal(root.get("category"), category));
            if (priceFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.quot(cb.prod(root.get("price"), cb.diff(BigDecimal.valueOf(100), root.get("discount"))),
                                BigDecimal.valueOf(100)).as(BigDecimal.class),
                        priceFrom));
            }
            if (priceTo != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        cb.quot(cb.prod(root.get("price"), cb.diff(BigDecimal.valueOf(100), root.get("discount"))),
                                BigDecimal.valueOf(100)).as(BigDecimal.class),
                        priceTo));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return PageData.of(tourRepository.findAll(spec, pageable).map(tourMapper::toCardResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public TourDetailResDTO getTourDetail(String slug) {
        Tour tour = tourRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour"));

        if (tour.getStatus() == TourStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour");
        }

        if (tour.getStatus() != TourStatus.ACTIVE && tour.getStatus() != TourStatus.ONGOING) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour");
        }
        if (tour.getStatus() == TourStatus.ONGOING ||
                (tour.getStatus() == TourStatus.ACTIVE &&
                        !(tour.getStartDate().isAfter(Instant.now()) && tour.getAvailableSlots() > 0))) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean isAuthorized = false;
            if (currentUserId != null) {
                if (currentUserId.equals(tour.getGuideId())) {
                    isAuthorized = true;
                } else {
                    isAuthorized = tourRepository.existsPaidOrderForUserAndTour(
                            currentUserId,
                            tour.getId());
                }
            }
            if (!isAuthorized) {
                throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour");
            }
        }
        TourDetailResDTO res = tourMapper.toDetailDto(tour);
        List<TourItinerary> itineraries = tourItineraryRepository
                .findByTourIdOrderByDayNumberAscPositionAsc(tour.getId());
        List<UUID> locIds = itineraries.stream()
                .filter(i -> i.getLocationId() != null)
                .map(TourItinerary::getLocationId)
                .collect(Collectors.toList());

        Map<UUID, Location> locationMap = locIds.isEmpty() ? Map.of()
                : locationRepository.findAllById(locIds).stream()
                        .collect(Collectors.toMap(Location::getId, Function.identity()));

        List<ItineraryItemResDTO> itineraryRes = itineraries.stream().map(it -> {
            ItineraryItemResDTO itemDto = tourMapper.toItineraryItemDto(it);
            if (it.getLocationId() != null) {
                Location loc = locationMap.get(it.getLocationId());
                if (loc != null) {
                    itemDto.setLocationName(loc.getName());
                    itemDto.setLocationImageUrl(loc.getImageUrl());
                    if (loc.getCoordinates() != null) {
                        itemDto.setLat(loc.getCoordinates().getY());
                        itemDto.setLng(loc.getCoordinates().getX());
                    }
                }
            }
            return itemDto;
        }).collect(Collectors.toList());
        res.setItineraries(itineraryRes);
        List<TourAddon> addons = tourAddonRepository.findByTourIdAndIsActiveTrue(tour.getId());
        if (tour.getGuideId() != null) {
            userRepository.findById(tour.getGuideId()).ifPresent(guideUser -> {
                res.setGuide(tourMapper.toGuideResDto(guideUser));
            });
        }
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourCardResponse> getUpcomingTours(Integer limit) {
        if (limit == null || limit <= 0)
            limit = 10;
        return tourRepository.findUpcomingNonDiscountedTours(limit).stream()
                .map(tourMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourCardResponse> getDiscountedTours(Integer limit) {
        if (limit == null || limit <= 0)
            limit = 10;
        return tourRepository.findUpcomingDiscountedTours(limit).stream()
                .map(tourMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "map_discovery")
    public List<MarkerResponse> getDiscoveryMap() {
        List<MarkerResponse> markers = new ArrayList<>();
        Specification<Tour> tourSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), TourStatus.ACTIVE));
            predicates.add(cb.greaterThan(root.get("availableSlots"), 0));
            predicates.add(cb.greaterThan(root.get("startDate"), Instant.now()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Tour> tours = tourRepository.findAll(tourSpec);
        if (!tours.isEmpty()) {
            List<UUID> tourIds = tours.stream().map(Tour::getId).collect(Collectors.toList());
            List<TourItinerary> allItineraries = tourItineraryRepository.findByTourIdIn(tourIds);

            Map<UUID, List<TourItinerary>> itinerariesByTour = allItineraries.stream()
                    .collect(Collectors.groupingBy(TourItinerary::getTourId));

            Map<UUID, UUID> tourToFirstLocationIdMap = new HashMap<>();
            for (Tour tour : tours) {
                List<TourItinerary> its = itinerariesByTour.get(tour.getId());
                if (its != null && !its.isEmpty()) {
                    // Tìm mục đầu tiên TRONG lịch trình mà CÓ locationId (không lấy các mục chỉ có text/mô tả)
                    TourItinerary firstWithLocation = its.stream()
                            .filter(it -> it.getLocationId() != null)
                            .min(Comparator.comparing(TourItinerary::getDayNumber)
                                    .thenComparing(TourItinerary::getPosition))
                            .orElse(null);

                    if (firstWithLocation != null) {
                        tourToFirstLocationIdMap.put(tour.getId(), firstWithLocation.getLocationId());
                    }
                }
            }
            List<UUID> locationIds = new ArrayList<>(new HashSet<>(tourToFirstLocationIdMap.values()));
            Map<UUID, Location> locationMap = locationIds.isEmpty() ? Map.of()
                    : locationRepository.findAllById(locationIds).stream()
                            .filter(loc -> loc.getDeletedAt() == null)
                            .collect(Collectors.toMap(Location::getId, Function.identity()));
            for (Tour tour : tours) {
                UUID locId = tourToFirstLocationIdMap.get(tour.getId());
                if (locId != null) {
                    Location loc = locationMap.get(locId);
                    if (loc != null && loc.getCoordinates() != null) {
                        markers.add(tourMapper.toMapMarkerDto(tour, loc));
                    }
                }
            }
        }
        // 2. Get Featured Location markers
        Specification<Location> locSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.greaterThan(root.get("priority"), 0));
            predicates.add(cb.lessThanOrEqualTo(root.get("featuredStartAt"), Instant.now()));
            predicates.add(cb.greaterThanOrEqualTo(root.get("featuredEndAt"), Instant.now()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Location> featuredLocs = locationRepository.findAll(locSpec);
        for (Location loc : featuredLocs) {
            if (loc.getCoordinates() != null) {
                markers.add(tourMapper.toAdMarkerDto(loc));
            }
        }
        
        // Sort markers by priority (descending, nulls last) and then by title
        markers.sort(Comparator.comparing((MarkerResponse m) -> m.getPriority() == null ? 0 : m.getPriority())
                .reversed()
                .thenComparing(m -> m.getTitle() == null ? "" : m.getTitle()));
                
        return markers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourVideoCardResponse> getTourVideoCards() {
        return tourRepository.findAllActiveWithShortId().stream()
                .map(tourMapper::toVideoCardResponse)
                .collect(Collectors.toList());
    }

}