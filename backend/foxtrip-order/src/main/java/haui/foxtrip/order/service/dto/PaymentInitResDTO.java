package haui.foxtrip.order.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResDTO {
    private String paymentUrl;
    private UUID orderId;
    private String orderCode;
    private Instant expiresAt;
}
