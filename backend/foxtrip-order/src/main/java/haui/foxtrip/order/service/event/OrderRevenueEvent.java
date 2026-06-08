package haui.foxtrip.order.service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderRevenueEvent {
    private final int month;
    private final int year;
    private final BigDecimal tourAmount;
    private final BigDecimal addonAmount;
    private final int orderCount;
}
