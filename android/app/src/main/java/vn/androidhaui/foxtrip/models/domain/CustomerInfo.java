package vn.androidhaui.foxtrip.models.domain;

public class CustomerInfo {
    private String username;
    private String phoneNumber;
    private String address;
    private String email;

    public String getUsername() { return username; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}