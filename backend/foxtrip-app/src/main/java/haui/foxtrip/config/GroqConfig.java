package haui.foxtrip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "foxtrip.groq")
public class GroqConfig {
    private String apiKey;
    private String apiUrl;
    private String model;
}
