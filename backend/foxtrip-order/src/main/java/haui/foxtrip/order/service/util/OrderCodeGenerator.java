package haui.foxtrip.order.service.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OrderCodeGenerator {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    /**
     * Generate order code with format: FT + YYMMDD + 6 random chars (A-Z, 0-9)
     * Example: FT260415A9BCX3
     * Timezone: Asia/Ho_Chi_Minh
     */
    public String generate() {
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        String dateStr = now.format(DATE_FORMATTER);
        String randomStr = generateRandomString();
        return "FT" + dateStr + randomStr;
    }
    
    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
