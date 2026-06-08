package haui.foxtrip.tour.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private SlugUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String nonAccent = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICAL_MARKS_PATTERN.matcher(nonAccent)
            .replaceAll("")
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    }
}
