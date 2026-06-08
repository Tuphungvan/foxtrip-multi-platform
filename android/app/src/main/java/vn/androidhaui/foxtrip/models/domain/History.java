package vn.androidhaui.foxtrip.models.domain;

import java.util.Date;
import java.util.List;

public class History {

    private String _id;
    private String orderId;
    private String userId;
    private CustomerInfo customerInfo;
    private Date completedAt;
    private Date endDate;
    private List<Item> items;
    private Date createdAt;
    private Date updatedAt;

    // Getter & Setter

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ---------- Inner classes ----------

    public static class CustomerInfo {
        private String username;
        private String phoneNumber;
        private String address;
        private String email;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getAddress() {
            return address;
        }
        public void setAddress(String address) {
            this.address = address;
        }

        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class Item {
        private String slug;
        private String name;
        private double price;
        private double discount;
        private double finalPrice;
        private String image;
        private int quantity;

        public String getSlug() {
            return slug;
        }
        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }
        public void setPrice(double price) {
            this.price = price;
        }

        public double getDiscount() {
            return discount;
        }
        public void setDiscount(double discount) {
            this.discount = discount;
        }

        public double getFinalPrice() {
            return finalPrice;
        }
        public void setFinalPrice(double finalPrice) {
            this.finalPrice = finalPrice;
        }

        public String getImage() {
            return image;
        }
        public void setImage(String image) {
            this.image = image;
        }

        public int getQuantity() {
            return quantity;
        }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
