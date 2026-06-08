package haui.foxtrip.tour.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.location.domain.Location;
import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.domain.TourItinerary;
import haui.foxtrip.tour.service.dto.request.TourAddonReqDTO;
import haui.foxtrip.tour.service.dto.request.TourAdminReqDTO;
import haui.foxtrip.tour.service.dto.request.TourItineraryReqDTO;
import haui.foxtrip.tour.service.dto.request.UpdateTourReqDTO;
import haui.foxtrip.tour.service.dto.request.RestartTourReqDTO;
import haui.foxtrip.tour.service.dto.response.GuideResDTO;
import haui.foxtrip.tour.service.dto.response.TourCreationResDTO;
import haui.foxtrip.tour.service.dto.response.TourDetailResDTO;
import haui.foxtrip.tour.service.dto.response.TourListResDTO;
import haui.foxtrip.tour.domain.enums.TourStatus;
import haui.foxtrip.tour.domain.enums.TourSetupStep;
import haui.foxtrip.tour.repository.TourAddonRepository;
import haui.foxtrip.tour.repository.TourItineraryRepository;
import haui.foxtrip.tour.repository.TourRepository;
import haui.foxtrip.tour.service.mapper.TourMapper;
import haui.foxtrip.tour.util.SlugUtil;
import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.domain.enums.Role;
import haui.foxtrip.user.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AdminTourServiceImpl implements AdminTourService {

    private final TourRepository tourRepository;
    private final TourItineraryRepository tourItineraryRepository;
    private final TourAddonRepository tourAddonRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional
    public TourCreationResDTO createTour(TourAdminReqDTO dto) {
        validateDateTour(dto.getStartDate(), dto.getEndDate());
        Tour tour = tourMapper.toTourEntity(dto);
        tour.setSlug(generateUniqueSlug(dto.getName()));
        tour.setAvailableSlots(dto.getSlots());
        tour.setStatus(TourStatus.HIDDEN);
        tour.setSetupStep(TourSetupStep.BASIC_DONE);
        tour.setReviewCount(0);
        return tourMapper.toCreationDto(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public TourDetailResDTO upsertItineraries(UUID tourId, TourItineraryReqDTO dto) {
        Tour tour = getValidTour(tourId);
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Lộ trình phải có ít nhất 1 địa điểm (Section IV)");
        }
        replaceItineraries(tourId, dto.getItems(), tourMapper::toItineraryEntity);
        if (tour.getSetupStep() == TourSetupStep.BASIC_DONE) {
            tour.setSetupStep(TourSetupStep.ITINERARY_DONE);
            tourRepository.save(tour);
        }
        return tourMapper.toDetailDto(tour);
    }

    @Override
    @Transactional
    public TourDetailResDTO upsertAddons(UUID tourId, TourAddonReqDTO dto) {
        Tour tour = getValidTour(tourId);
        if (tour.getSetupStep().ordinal() < TourSetupStep.ITINERARY_DONE.ordinal()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Vui lòng thiết lập Lịch trình trước.");
        }
        replaceAddons(tourId, dto.getAddons(), tourMapper::toAddonEntity);
        if (tour.getSetupStep() == TourSetupStep.ITINERARY_DONE) {
            tour.setSetupStep(TourSetupStep.ADDON_DONE);
            tourRepository.save(tour);
        }
        return tourMapper.toDetailDto(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideResDTO> getGuideSuggestions(UUID tourId, String startDate, String endDate) {
        Tour tour = getValidTour(tourId);
        // Ưu tiên dùng ngày mới từ frontend nếu có (khi restart), fallback về ngày tour
        Instant start = (startDate != null && !startDate.isBlank())
                ? Instant.parse(startDate)
                : tour.getStartDate();
        Instant end = (endDate != null && !endDate.isBlank())
                ? Instant.parse(endDate)
                : tour.getEndDate();
        List<User> availableGuides = tourRepository.findAvailableGuides(start, end);
        return availableGuides.stream()
                .limit(10)
                .map(tourMapper::toGuideResDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public TourDetailResDTO assignGuide(UUID tourId, UUID guideId) {
        Tour tour = getValidTour(tourId);
        if (tour.getSetupStep().ordinal() < TourSetupStep.ADDON_DONE.ordinal()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Vui lòng thiết lập Lịch trình và Dịch vụ trước.");
        }
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Guide"));
        if (guide.getRole() != Role.GUIDE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Người dùng này không phải GUIDE");
        }
        checkGuideConflict(guideId, tour.getStartDate(), tour.getEndDate(),
                "Guide đã được phân công cho Tour khác trùng lịch. Vui lòng chọn lại.");
        tour.setGuideId(guideId);
        if (tour.getSetupStep() == TourSetupStep.ADDON_DONE) {
            tour.setSetupStep(TourSetupStep.READY);
        }
        tour.setStatus(TourStatus.ACTIVE);
        return tourMapper.toDetailDto(tourRepository.save(tour));
    }

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public TourListResDTO updateTour(UUID tourId, UpdateTourReqDTO dto) {
        Tour tour = getValidTour(tourId);
        if (tour.getStatus() == TourStatus.ONGOING || tour.getStatus() == TourStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Tour đang diễn ra hoặc đã kết thúc, không thể sửa trực tiếp.");
        }
        if (tour.getStatus() == TourStatus.ACTIVE) {
            if (dto.getName() != null || dto.getProvince() != null || dto.getCategory() != null ||
                    dto.getSlots() != null || dto.getStartDate() != null || dto.getEndDate() != null
                    || dto.getPrice() != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                        "Tour đang hoạt động, chỉ được sửa Hướng dẫn viên, Giảm giá, Ảnh bìa, Video short.");
            }
        }
        Instant newStartDate = dto.getStartDate() != null ? dto.getStartDate() : tour.getStartDate();
        Instant newEndDate = dto.getEndDate() != null ? dto.getEndDate() : tour.getEndDate();
        validateDateTour(newStartDate, newEndDate);

        if (tour.getGuideId() != null && (dto.getStartDate() != null || dto.getEndDate() != null)) {
            checkGuideConflict(tour.getGuideId(), newStartDate, newEndDate,
                    "Guide bị trùng lịch, vui lòng chọn guide khác");
        }

        if (dto.getSlots() != null) {
            tour.setSlots(dto.getSlots());
            tour.setAvailableSlots(dto.getSlots());
        }

        tourMapper.updateTourFromDTO(dto, tour);
        return tourMapper.toListDto(tourRepository.save(tour));
    }

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public TourDetailResDTO restartTour(UUID tourId, RestartTourReqDTO dto) {
        Tour tour = getValidTour(tourId);
        if (tour.getStatus() != TourStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Chỉ có thể khởi động lại tour đã hoàn tất");
        }
        validateDateTour(dto.getStartDate(), dto.getEndDate());
        // Kiểm tra guide không bị trùng lịch với tour khác
        if (dto.getGuideId() != null) {
            checkGuideConflict(dto.getGuideId(), dto.getStartDate(), dto.getEndDate(),
                    "Hướng dẫn viên đã có tour khác trùng lịch. Vui lòng chọn người khác.");
        }
        tourMapper.updateTourFromRestartDTO(dto, tour);
        tour.setAvailableSlots(dto.getSlots());
        tour.setStatus(TourStatus.ACTIVE);
        tour.setSetupStep(TourSetupStep.READY);
        return tourMapper.toDetailDto(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public void deleteTour(UUID tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour"));
        if (tour.getStatus() != TourStatus.HIDDEN && tour.getStatus() != TourStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Chỉ có thể xóa tour ở trạng thái HIDDEN hoặc COMPLETED");
        }
        tour.setDeletedAt(Instant.now());
        tourRepository.save(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public TourDetailResDTO getTour(UUID tourId) {
        Tour tour = getValidTour(tourId);
        return tourMapper.toDetailDto(tour);
    }

    @Override
    @Transactional
    public void restoreTour(UUID tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour"));

        tour.setDeletedAt(null);
        tourRepository.save(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<TourListResDTO> getTours(String keyword, String type, TourStatus status, int page, int size) {
        Specification<Tour> spec = buildSpec(keyword, type, status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Tour> result = tourRepository.findAll(spec, pageable);
        return PageData.of(result.map(tourMapper::toListDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<TourListResDTO> getToursByGuide(UUID guideId, Pageable pageable) {
        Specification<Tour> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("guideId"), guideId));
            predicates.add(root.get("status").in(TourStatus.ACTIVE, TourStatus.ONGOING));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Tour> result = tourRepository.findAll(spec, pageable);
        return PageData.of(result.map(tourMapper::toListDto));
    }

    private Specification<Tour> buildSpec(String keyword, String type, TourStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), kw));
            }
            if (type != null && !type.trim().isEmpty()) {
                switch (type.toUpperCase()) {
                    case "ACTIVE" -> predicates.add(cb.isNull(root.get("deletedAt")));
                    case "DELETED" -> predicates.add(cb.isNotNull(root.get("deletedAt")));
                }
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Tour getValidTour(UUID tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy Tour"));
        if (tour.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.GONE.value(), "Tour đã bị xóa");
        }
        return tour;
    }

    private void validateDateTour(Instant startDate, Instant endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.toSlug(name);
        String slug;
        Random random = new Random();
        do {
            slug = baseSlug + "-" + String.format("%06d", random.nextInt(1000000));
        } while (tourRepository.existsBySlug(slug));
        return slug;
    }

    private void checkGuideConflict(UUID guideId, Instant startDate, Instant endDate, String errorMessage) {
        boolean hasConflict = tourRepository.existsOverlappingTour(guideId, startDate, endDate);
        if (hasConflict) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), errorMessage);
        }
    }

    private <T> void replaceItineraries(UUID tourId, Collection<T> itemReqs, Function<T, TourItinerary> mapper) {
        if (itemReqs == null || itemReqs.isEmpty()) {
            return;
        }
        tourItineraryRepository.deleteByTourId(tourId);

        List<TourItinerary> itineraries = itemReqs.stream()
                .map(mapper)
                .peek(item -> item.setTourId(tourId))
                .toList();

        // Optimized Location Validation: Collect unique IDs and batch check
        Set<UUID> locationIds = itineraries.stream()
                .map(TourItinerary::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!locationIds.isEmpty()) {
            List<Location> foundLocations = locationRepository.findAllById(locationIds);
            if (foundLocations.size() != locationIds.size()) {
                throw new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Một hoặc nhiều địa điểm không tồn tại hoặc đã bị xóa.");
            }
        }

        tourItineraryRepository.saveAll(itineraries);
    }

    private <T> void replaceAddons(UUID tourId, Collection<T> addonReqs, Function<T, TourAddon> mapper) {
        if (addonReqs == null || addonReqs.isEmpty()) {
            return;
        }
        tourAddonRepository.deleteByTourId(tourId);

        List<TourAddon> addons = addonReqs.stream().map(req -> {
            TourAddon addon = mapper.apply(req);
            addon.setTourId(tourId);
            return addon;
        }).collect(Collectors.toList());
        tourAddonRepository.saveAll(addons);
    }
}