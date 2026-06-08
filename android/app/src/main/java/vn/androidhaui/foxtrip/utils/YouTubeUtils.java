package vn.androidhaui.foxtrip.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeUtils {

    public static String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isEmpty()) return null;

        // More robust pattern to handle hyphens and underscores in IDs
        String pattern = "(?<=watch\\?v=|/videos/|/embed/|youtu.be/|/shorts/|\\?v=|^v=)([a-zA-Z0-9_-]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youtubeUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Fallback for cases like youtube.com/v/VIDEO_ID
        if (youtubeUrl.contains("/v/")) {
            String[] parts = youtubeUrl.split("/v/");
            if (parts.length > 1) {
                return parts[1].split("[?&]")[0];
            }
        }

        return null;
    }
}
