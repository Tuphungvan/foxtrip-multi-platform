package vn.androidhaui.foxtrip.models.dto.response;

import java.io.Serializable;

public class PassengerResDTO implements Serializable {
    public String orderCode;
    public String customerName;
    public String customerPhone;
    public String customerEmail;
    public Integer quantity;
    public String status;
    public boolean checkedIn;
}
