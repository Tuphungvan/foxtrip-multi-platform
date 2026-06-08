package haui.foxtrip.config;

import haui.foxtrip.user.security.TokenProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@SuppressWarnings("unused")
public class SecurityJwtConfiguration {

    private final TokenProvider tokenProvider;
    private final CacheManager cacheManager;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(tokenProvider.getPublicKey()).build();
        return token -> {
            Jwt jwt = jwtDecoder.decode(token);
            validateSession(jwt);
            return jwt;
        };
    }

    private void validateSession(Jwt jwt) {
        String sessionId = jwt.getClaimAsString("sid");
        String subject = jwt.getSubject();
        if (sessionId == null || sessionId.isBlank() || subject == null || subject.isBlank()) {
            throw new JwtException("Phiên không hợp lệ");
        }
        Cache cache = cacheManager.getCache("user-session");
        if (cache == null) {
            throw new JwtException("Không thể kiểm tra phiên");
        }
        String cachedSession = cache.get(subject, String.class);
        if (cachedSession == null || !cachedSession.equals(sessionId)) {
            throw new JwtException("Phiên đã hết hạn");
        }
    }
}
