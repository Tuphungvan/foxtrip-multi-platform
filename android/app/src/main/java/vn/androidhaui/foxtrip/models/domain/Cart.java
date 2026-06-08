package vn.androidhaui.foxtrip.models.domain;

import java.util.List;

public class Cart {

    private String userId;
    private List<CartItem> items;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
