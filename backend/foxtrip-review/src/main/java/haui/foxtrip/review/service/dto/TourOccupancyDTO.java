package haui.foxtrip.review.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourOccupancyDTO {
    private UUID tourId;
    private String tourName;
    private long runCount; // Số lần tổ chức (tính theo unique start_date_at_time)
    private long totalSlots;
    private long bookedSlots;
    private BigDecimal averageOccupancy;
}
