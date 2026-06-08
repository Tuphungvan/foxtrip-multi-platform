package haui.foxtrip.review.service;

import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.order.repository.OrderRepository;
import haui.foxtrip.review.service.dto.DailyBookingDTO;
import haui.foxtrip.review.service.dto.TourOccupancyDTO;
import haui.foxtrip.tour.domain.Tour;
import haui.foxtrip.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatisticServiceImpl implements AdminStatisticService {

    private final OrderRepository orderRepository;
    private final TourRepository tourRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TourOccupancyDTO> getTourOccupancyReport(Integer month, Integer year) {
        Instant since;
        Instant until;
        
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        if (month != null && year != null) {
            ZonedDateTime start = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, vnZone);
            ZonedDateTime end = start.plusMonths(1);
            since = start.toInstant();
            until = end.toInstant();
        } else if (year != null) {
            ZonedDateTime start = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, vnZone);
            ZonedDateTime end = start.plusYears(1);
            since = start.toInstant();
            until = end.toInstant();
        } else {
            since = Instant.now().minus(120, ChronoUnit.DAYS);
            until = Instant.now().plus(365, ChronoUnit.DAYS);
        }

        List<Object[]> rawData = orderRepository.findTourOccupancyRaw(since, until);

        // Map to store aggregated data per Tour ID
        Map<UUID, TourOccupancyDTO> occupancyMap = new HashMap<>();

        for (Object[] row : rawData) {
            UUID tourId = (UUID) row[0];
            String tourName = (String) row[1];
            // start_date_at_time is row[2] - we don't need it for aggregate but it defines the run
            long quantity = ((Number) row[3]).longValue();

            TourOccupancyDTO dto = occupancyMap.computeIfAbsent(tourId, k -> TourOccupancyDTO.builder()
                    .tourId(tourId)
                    .tourName(tourName)
                    .runCount(0)
                    .totalSlots(0)
                    .bookedSlots(0)
                    .build());

            dto.setRunCount(dto.getRunCount() + 1);
            dto.setBookedSlots(dto.getBookedSlots() + quantity);
        }

        // Fetch slots for each tour to calculate total capacity and occupancy rate
        List<UUID> tourIds = new ArrayList<>(occupancyMap.keySet());
        if (!tourIds.isEmpty()) {
            List<Tour> tours = tourRepository.findAllById(tourIds);
            Map<UUID, Integer> tourSlotsMap = tours.stream()
                    .collect(Collectors.toMap(Tour::getId, Tour::getSlots));

            for (TourOccupancyDTO dto : occupancyMap.values()) {
                Integer slots = tourSlotsMap.getOrDefault(dto.getTourId(), 0);
                long capacity = dto.getRunCount() * slots;
                dto.setTotalSlots(capacity);

                if (capacity > 0) {
                    BigDecimal rate = BigDecimal.valueOf(dto.getBookedSlots())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(capacity), 2, RoundingMode.HALF_UP);
                    dto.setAverageOccupancy(rate);
                } else {
                    dto.setAverageOccupancy(BigDecimal.ZERO);
                }
            }
        }

        return occupancyMap.values().stream()
                .sorted(Comparator.comparing(TourOccupancyDTO::getAverageOccupancy).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyBookingDTO> getDailyBookingReport(String dateStr) {
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate date = (dateStr == null || dateStr.isEmpty()) 
                ? LocalDate.now(vnZone) 
                : LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        
        Instant start = date.atStartOfDay(vnZone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(vnZone).toInstant().minus(1, ChronoUnit.MILLIS);

        List<Object[]> rawData = orderRepository.findDailyBookingsRaw(start, end);

        return rawData.stream().map(row -> DailyBookingDTO.builder()
                .tourId((UUID) row[0])
                .tourName((String) row[1])
                .bookingCount(((Number) row[2]).longValue())
                .totalQuantity(((Number) row[3]).longValue())
                .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countFeaturedLocations(Integer month, Integer year) {
        Instant since;
        Instant until;
        
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        if (month != null && year != null) {
            ZonedDateTime start = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, vnZone);
            ZonedDateTime end = start.plusMonths(1);
            since = start.toInstant();
            until = end.toInstant();
        } else if (year != null) {
            ZonedDateTime start = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, vnZone);
            ZonedDateTime end = start.plusYears(1);
            since = start.toInstant();
            until = end.toInstant();
        } else {
            since = Instant.now().minus(30, ChronoUnit.DAYS);
            until = Instant.now();
        }
        
        return locationRepository.countFeaturedInPeriod(since, until);
    }
}
