package vn.androidhaui.foxtrip.models.dto.request;

public class VerifyEmailRequest {
    public final String email;
    public final String otp;

    public VerifyEmailRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}