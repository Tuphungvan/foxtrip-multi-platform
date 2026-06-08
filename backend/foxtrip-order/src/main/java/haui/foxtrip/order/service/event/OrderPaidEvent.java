package haui.foxtrip.order.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaidEvent {
    private UUID orderId;
    private String orderCode;
}
