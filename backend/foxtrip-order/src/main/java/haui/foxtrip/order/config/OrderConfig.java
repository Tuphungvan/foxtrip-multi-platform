package haui.foxtrip.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "foxtrip.order")
public class OrderConfig {
    private int expirationHours = 24;
}
