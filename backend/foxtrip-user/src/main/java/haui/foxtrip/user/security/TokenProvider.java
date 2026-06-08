package haui.foxtrip.user.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import haui.foxtrip.user.config.JwtProperties;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private long accessTokenValiditySeconds;
    private long refreshTokenValiditySeconds;

    @PostConstruct
    void init() {
        this.accessTokenValiditySeconds = jwtProperties.getAccessTokenValiditySeconds();
        this.refreshTokenValiditySeconds = jwtProperties.getRefreshTokenValiditySeconds();
        this.privateKey = loadPrivateKey(jwtProperties.getPrivateKey());
        this.publicKey = loadPublicKey(jwtProperties.getPublicKey());
    }

    public String createAccessToken(String subject, UUID userId, List<String> authorities,
                                    String sessionId, boolean verified) {
        var now = new Date();
        var exp = new Date(now.getTime() + accessTokenValiditySeconds * 1000);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(now)
                .expirationTime(exp)
                .claim("authorities", authorities)
                .claim("userId", userId.toString())
                .claim("verified", verified)
                .claim("sid", sessionId)
                .claim("token_type", "access")
                .build();
        return signAndSerialize(claims);
    }

    public String createRefreshToken(String subject, String sessionId) {
        var now = new Date();
        var exp = new Date(now.getTime() + refreshTokenValiditySeconds * 1000);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(now)
                .expirationTime(exp)
                .claim("sid", sessionId)
                .claim("token_type", "refresh")
                .build();
        return signAndSerialize(claims);
    }

    private String signAndSerialize(JWTClaimsSet claims) {
        try {
            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims);
            signedJWT.sign(new RSASSASigner(privateKey));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private RSAPrivateKey loadPrivateKey(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException("JWT private key is missing. Set application.jwt.private-key.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load JWT private key", ex);
        }
    }

    private RSAPublicKey loadPublicKey(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException("JWT public key is missing. Set application.jwt.public-key.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load JWT public key", ex);
        }
    }
}
