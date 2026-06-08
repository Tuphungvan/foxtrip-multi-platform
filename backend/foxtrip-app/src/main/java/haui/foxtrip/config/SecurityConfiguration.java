package haui.foxtrip.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.fasterxml.jackson.databind.ObjectMapper;
import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.security.JwtAuthenticationConverter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableMethodSecurity
@SuppressWarnings("unused")
public class SecurityConfiguration {

    private record HttpMethodPattern(HttpMethod method, String pattern) {}

    private static final HttpMethodPattern[] PUBLIC_AUTH_ENDPOINTS = {
            new HttpMethodPattern(HttpMethod.POST, "/api/auth/**")
    };

    private static final String[] PUBLIC_DOCS_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health/**"
    };

    private static final HttpMethodPattern[] PUBLIC_DATA_ENDPOINTS = {
            new HttpMethodPattern(HttpMethod.GET, "/api/tours/**"),
            new HttpMethodPattern(HttpMethod.GET, "/api/map/**"),
            new HttpMethodPattern(HttpMethod.GET, "/api/locations/**")
    };

    private static final HttpMethodPattern[] PUBLIC_GUEST_ENDPOINTS = {
            new HttpMethodPattern(HttpMethod.POST, "/api/chat")
    };

    private static final String[] PUBLIC_PAYMENT_WEBHOOKS = {
            "/api/payments/vnpay/ipn",
            "/api/payments/vnpay/return"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc, ObjectMapper objectMapper) throws Exception {
        http.cors(withDefaults())
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> {
                    for (HttpMethodPattern ep : PUBLIC_AUTH_ENDPOINTS) {
                        authz.requestMatchers(mvc.pattern(ep.method(), ep.pattern())).permitAll();
                    }
                    for (String pattern : PUBLIC_DOCS_ENDPOINTS) {
                        authz.requestMatchers(new AntPathRequestMatcher(pattern)).permitAll();
                    }
                    for (HttpMethodPattern ep : PUBLIC_DATA_ENDPOINTS) {
                        authz.requestMatchers(mvc.pattern(ep.method(), ep.pattern())).permitAll();
                    }
                    for (HttpMethodPattern ep : PUBLIC_GUEST_ENDPOINTS) {
                        authz.requestMatchers(mvc.pattern(ep.method(), ep.pattern())).permitAll();
                    }
                    for (String pattern : PUBLIC_PAYMENT_WEBHOOKS) {
                        authz.requestMatchers(new AntPathRequestMatcher(pattern, "GET")).permitAll();
                    }
                    authz.anyRequest().authenticated();
                })
                .httpBasic(withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter())))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> writeError(response,
                                objectMapper, HttpStatus.UNAUTHORIZED, "Chưa xác thực."))
                        .accessDeniedHandler((request, response, accessDeniedException) -> writeError(response,
                                objectMapper, HttpStatus.FORBIDDEN, "Không có quyền truy cập.")));
        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(HttpServletResponse response, ObjectMapper objectMapper, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(message);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
