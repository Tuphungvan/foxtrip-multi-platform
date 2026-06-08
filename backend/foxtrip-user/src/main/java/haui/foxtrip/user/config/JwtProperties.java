package haui.foxtrip.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.jwt")
public class JwtProperties {
    private long accessTokenValiditySeconds;
    private long refreshTokenValiditySeconds;
    private String privateKey;
    private String publicKey;
}
