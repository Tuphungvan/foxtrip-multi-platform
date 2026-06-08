package haui.foxtrip.user.util;

import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OtpFormatUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    public static String generateOtp() {
        int otp = RANDOM.nextInt(1_000_000);
        return String.format("%06d", otp);
    }
}