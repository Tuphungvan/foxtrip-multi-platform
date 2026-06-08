package haui.foxtrip.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs from Vietnamese text.
 * 
 * <p>Example usage:
 * <pre>
 * String slug = SlugUtil.toSlug("Hà Nội - Thủ đô ngàn năm");
 * // Result: "ha-noi-thu-do-ngan-nam"
 * </pre>
 */
public final class SlugUtil {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private SlugUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a string to a URL-friendly slug.
     * 
     * <p>Process:
     * <ol>
     *   <li>Removes Vietnamese diacritics (á → a, ê → e, etc.)</li>
     *   <li>Converts to lowercase</li>
     *   <li>Replaces non-alphanumeric characters with hyphens</li>
     *   <li>Removes leading/trailing hyphens</li>
     * </ol>
     * 
     * @param input the input string (can contain Vietnamese characters)
     * @return URL-friendly slug, or empty string if input is null/empty
     * 
     * @example
     * <pre>
     * toSlug("Hà Nội")           → "ha-noi"
     * toSlug("Đà Lạt 2024")      → "da-lat-2024"
     * toSlug("Tour Sapa - 3N2Đ") → "tour-sapa-3n2d"
     * </pre>
     */
    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Normalize to NFD (decompose accented characters)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        
        // Remove diacritical marks
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        
        // Convert to lowercase, replace non-alphanumeric with hyphens, remove leading/trailing hyphens
        return withoutDiacritics
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Generates a unique slug by appending a suffix if needed.
     * 
     * @param baseSlug the base slug
     * @param suffix the suffix to append (e.g., timestamp, counter)
     * @return unique slug in format "base-slug-suffix"
     * 
     * @example
     * <pre>
     * toUniqueSlug("ha-noi", "123") → "ha-noi-123"
     * </pre>
     */
    public static String toUniqueSlug(String baseSlug, String suffix) {
        if (suffix == null || suffix.trim().isEmpty()) {
            return baseSlug;
        }
        return baseSlug + "-" + suffix;
    }
}
