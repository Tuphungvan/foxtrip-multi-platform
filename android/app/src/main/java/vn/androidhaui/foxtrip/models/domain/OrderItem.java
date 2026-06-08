package vn.androidhaui.foxtrip.models.domain;

public class OrderItem {
    private String slug;
    private String name;
    private Double price;
    private Integer discount;
    private Double finalPrice;
    private String image;
    private Integer quantity;

    public OrderItem() {}

    public OrderItem(String name, Double finalPrice, Integer quantity, String image) {
        this.name = name;
        this.finalPrice = finalPrice;
        this.quantity = quantity;
        this.image = image;
    }

    public void setSlug(String slug) { this.slug = slug; }
    public void setName(String name) { this.name = name; }
    public void setPrice(Double price) { this.price = price; }
    public void setDiscount(Integer discount) { this.discount = discount; }
    public void setFinalPrice(Double finalPrice) { this.finalPrice = finalPrice; }
    public void setImage(String image) { this.image = image; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSlug() { return slug; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Integer getDiscount() { return discount != null ? discount : 0; }
    public Double getFinalPrice() { return finalPrice != null ? finalPrice : 0.0; }
    public String getImage() { return image; }
    public Integer getQuantity() { return quantity != null ? quantity : 0; }
}
