package haui.foxtrip.location.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.location.domain.Location;
import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.location.service.dto.request.CreateLocationRequest;
import haui.foxtrip.location.service.dto.request.UpdateLocationRequest;
import haui.foxtrip.location.service.dto.response.LocationResponse;
import haui.foxtrip.location.service.event.AdsRevenueEvent;
import haui.foxtrip.location.service.mapper.LocationMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AdminLocationServiceImpl implements AdminLocationService {

    private static final java.math.BigDecimal PRICE_PRIORITY_1 = new java.math.BigDecimal("50000");
    private static final java.math.BigDecimal PRICE_PRIORITY_2 = new java.math.BigDecimal("100000");
    private static final java.math.BigDecimal PRICE_PRIORITY_3 = new java.math.BigDecimal("200000");

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public LocationResponse createLocation(CreateLocationRequest dto) {
        locationRepository.findByNameAndProvinceAndAddress(dto.getName(), dto.getProvince(), dto.getAddress())
                .ifPresent(ex -> {
                    throw new BusinessException(HttpStatus.CONFLICT.value(), "Địa điểm đã tồn tại");
                });

        // Validate advertisement start date
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (dto.getFeaturedStartAt() != null && dto.getFeaturedStartAt().isBefore(today)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Ngày bắt đầu quảng cáo không được nhỏ hơn ngày hôm nay");
        }

        Location location = locationMapper.toEntity(dto);
        Location saved = locationRepository.save(location);

        // Calculate and record ads revenue if featured
        if (saved.getFeaturedStartAt() != null && saved.getFeaturedEndAt() != null && saved.getPriority() != null && saved.getPriority() > 0) {
            recordAdsRevenue(saved);
        }

        return locationMapper.toDto(saved);
    }

    private BigDecimal calculateAdsAmount(Location location) {
        BigDecimal pricePerDay = switch (location.getPriority()) {
            case 1 -> PRICE_PRIORITY_1;
            case 2 -> PRICE_PRIORITY_2;
            case 3 -> PRICE_PRIORITY_3;
            default -> BigDecimal.ZERO;
        };

        if (pricePerDay.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        long days = ChronoUnit.DAYS.between(location.getFeaturedStartAt(), location.getFeaturedEndAt()) + 1;
        return days > 0 ? pricePerDay.multiply(BigDecimal.valueOf(days)) : BigDecimal.ZERO;
    }

    private void recordAdsRevenue(Location location) {
        BigDecimal totalAmount = calculateAdsAmount(location);
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            ZonedDateTime zdt = location.getFeaturedStartAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            eventPublisher.publishEvent(new AdsRevenueEvent(zdt.getMonthValue(), zdt.getYear(), totalAmount));
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public LocationResponse updateLocation(UUID id, UpdateLocationRequest dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy địa điểm"));
        
        locationRepository.findByNameAndProvinceAndAddress(dto.getName(), dto.getProvince(), dto.getAddress())
                .filter(ex -> !ex.getId().equals(id))
                .ifPresent(ex -> {
                    throw new BusinessException(HttpStatus.CONFLICT.value(), "Địa điểm trùng với một bản ghi khác");
                });

        boolean wasFeatured = location.getFeaturedStartAt() != null && location.getFeaturedEndAt() != null && location.getPriority() != null && location.getPriority() > 0;
        BigDecimal oldAmount = wasFeatured ? calculateAdsAmount(location) : BigDecimal.ZERO;
        Instant oldStart = location.getFeaturedStartAt();
        Instant oldEnd = location.getFeaturedEndAt();

        // Business Rules Validation
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        
        // 1. Validation for Start Date
        if (dto.getFeaturedStartAt() != null) {
            if (oldStart == null) {
                if (dto.getFeaturedStartAt().isBefore(today)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Ngày bắt đầu quảng cáo không được nhỏ hơn ngày hôm nay");
                }
            } else if (!dto.getFeaturedStartAt().truncatedTo(ChronoUnit.MINUTES).equals(oldStart.truncatedTo(ChronoUnit.MINUTES))) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Không được phép thay đổi ngày bắt đầu quảng cáo đã thiết lập");
            }
        }

        // 2. Validation for End Date (Only allow extension)
        if (dto.getFeaturedEndAt() != null && oldEnd != null) {
            if (dto.getFeaturedEndAt().truncatedTo(ChronoUnit.MINUTES).isBefore(oldEnd.truncatedTo(ChronoUnit.MINUTES))) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Ngày kết thúc quảng cáo chỉ được phép gia hạn thêm, không được rút ngắn");
            }
        }

        locationMapper.updateEntityFromDto(dto, location);
        
        // Manually set protected advertisement fields after validation
        if (dto.getFeaturedStartAt() != null) {
            location.setFeaturedStartAt(dto.getFeaturedStartAt());
        }
        if (dto.getFeaturedEndAt() != null) {
            location.setFeaturedEndAt(dto.getFeaturedEndAt());
        }
        if (dto.getPriority() != null) {
            location.setPriority(dto.getPriority());
        }

        Location saved = locationRepository.save(location);

        boolean isFeatured = saved.getFeaturedStartAt() != null && saved.getFeaturedEndAt() != null && saved.getPriority() != null && saved.getPriority() > 0;

        if (isFeatured) {
            BigDecimal newAmount = calculateAdsAmount(saved);
            // Only charge the difference
            if (newAmount.compareTo(oldAmount) > 0) {
                BigDecimal diff = newAmount.subtract(oldAmount);
                ZonedDateTime zdt = saved.getFeaturedStartAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
                eventPublisher.publishEvent(new AdsRevenueEvent(zdt.getMonthValue(), zdt.getYear(), diff));
                log.info("Updated ads revenue for location {}: Added diff amount = {}", saved.getName(), diff);
            }
        }

        return locationMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public void deleteLocation(UUID id) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy hoặc đã bị xóa"));
        
        // Check if location is currently featured
        Instant now = Instant.now();
        if (location.getFeaturedStartAt() != null && location.getFeaturedEndAt() != null) {
            if (now.isAfter(location.getFeaturedStartAt()) && now.isBefore(location.getFeaturedEndAt())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Địa điểm đang trong thời gian quảng cáo, không thể xóa");
            }
        }

        Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM tour_itineraries WHERE location_id = :id")
                .setParameter("id", id)
                .getSingleResult();
        if (count.longValue() > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Địa điểm đang được dùng trong lịch trình tour");
        }
        location.setDeletedAt(Instant.now());
        locationRepository.save(location);
    }

    @Override
    @Transactional
    @CacheEvict(value = "map_discovery", allEntries = true)
    public void restoreLocation(UUID id) {
        Location location = locationRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy địa điểm đã bị xóa"));
        location.setDeletedAt(null);
        locationRepository.save(location);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<LocationResponse> getLocations(String keyword, String type, int page, int size) {
        Specification<Location> spec = buildSpec(keyword, type);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Location> result = locationRepository.findAll(spec, pageable);
        return PageData.of(
                result.map(locationMapper::toDto)
        );
    }

    private Specification<Location> buildSpec(String keyword, String type) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(
                        cb.like(cb.lower(root.get("name")), kw)
                );
            }
            if (type != null && !type.trim().isEmpty()) {
                switch (type.toUpperCase()) {
                    case "ACTIVE" ->
                            predicates.add(cb.isNull(root.get("deletedAt")));
                    case "DELETED" ->
                            predicates.add(cb.isNotNull(root.get("deletedAt")));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}