package haui.foxtrip.review.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBookingDTO {
    private UUID tourId;
    private String tourName;
    private long bookingCount; // Số lượng đơn đặt trong ngày
    private long totalQuantity; // Tổng số chỗ đã đặt trong ngày
}
