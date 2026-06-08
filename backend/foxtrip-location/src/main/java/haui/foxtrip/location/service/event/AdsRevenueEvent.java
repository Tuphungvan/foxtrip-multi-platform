package haui.foxtrip.location.service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AdsRevenueEvent {
    private final int month;
    private final int year;
    private final BigDecimal amount;
}
