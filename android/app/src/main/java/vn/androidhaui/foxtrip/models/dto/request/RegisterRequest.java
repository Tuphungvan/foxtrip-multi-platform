package vn.androidhaui.foxtrip.models.dto.request;

public class RegisterRequest {
    public final String email;
    public final String password;
    public final String username;
    public final String phoneNumber;
    public final boolean sendOtp;

    public RegisterRequest(String email, String password, String username,
                           String phoneNumber, boolean sendOtp) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.sendOtp = sendOtp;
    }
}
