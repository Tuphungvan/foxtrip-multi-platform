package haui.foxtrip.user.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailNormalizeUtil {

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}