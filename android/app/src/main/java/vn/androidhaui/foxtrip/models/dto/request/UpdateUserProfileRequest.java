package vn.androidhaui.foxtrip.models.dto.request;

public class UpdateUserProfileRequest {
    private String username;
    private String phoneNumber;
    private String password;
    private String avatarUrl;

    public UpdateUserProfileRequest() {
    }

    public UpdateUserProfileRequest(String username, String phoneNumber, String password, String avatarUrl) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.avatarUrl = avatarUrl;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
