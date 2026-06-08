package haui.foxtrip.user.service;

public interface AuthEmailService {

    void sendOtpRegister(String email);

    void verifyEmail(String email, String otp);

    boolean resendOtpIfNotVerified(String email);

    boolean resendOtpForCurrentUser();

    void verifyOtpForCurrentUser(String otp);

    void sendPasswordResetEmail(String email, String tempPassword);

    void sendGuideAccountEmail(String email, String password);
}
