package haui.foxtrip.order.service.dto;

import haui.foxtrip.order.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerResDTO {
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer quantity;
    private OrderStatus status;
    private boolean checkedIn;

    public PassengerResDTO(String orderCode, String customerName, String customerPhone, String customerEmail, Integer quantity, OrderStatus status) {
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.quantity = quantity;
        this.status = status;
        this.checkedIn = false;
    }
}
