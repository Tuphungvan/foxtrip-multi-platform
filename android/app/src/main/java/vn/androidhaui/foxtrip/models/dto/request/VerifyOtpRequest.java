package vn.androidhaui.foxtrip.models.dto.request;

public class VerifyOtpRequest {
    private String otp;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
