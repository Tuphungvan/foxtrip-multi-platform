package haui.foxtrip.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "foxtrip.qr")
public class QRConfig {
    private String secretKey;
}
