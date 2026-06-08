package haui.foxtrip.user.util;

import lombok.experimental.UtilityClass;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class GeneratePasswordUtil {
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateTempPassword() {
        List<Character> chars = new ArrayList<>();
        chars.add(randomChar("abcdefghijklmnopqrstuvwxyz"));
        chars.add(randomChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        chars.add(randomChar("0123456789"));
        chars.add(randomChar("!@#$%^&*()-_=+[]{}"));

        String all = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}";
        while (chars.size() < TEMP_PASSWORD_LENGTH) {
            chars.add(randomChar(all));
        }
        Collections.shuffle(chars, SECURE_RANDOM);
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }
    private char randomChar(String pool) {
        return pool.charAt(SECURE_RANDOM.nextInt(pool.length()));
    }
}