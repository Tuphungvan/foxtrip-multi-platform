package vn.androidhaui.foxtrip.models.domain;

import java.util.Objects;

public class CartItem {
    private String slug;
    private String name;
    private Double price;
    private String image;
    private Integer quantity;
    private Integer discount;
    private Double finalPrice;

    // getters / setters
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getQuantity() { return quantity != null ? quantity : 0; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getDiscount() { return discount != null ? discount : 0; }
    public void setDiscount(Integer discount) { this.discount = discount; }

    public Double getFinalPrice() { return finalPrice != null ? finalPrice : 0.0; }
    public void setFinalPrice(Double finalPrice) { this.finalPrice = finalPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(getSlug(), cartItem.getSlug());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSlug());
    }
}
