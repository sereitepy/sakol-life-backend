package com.sakollife.config;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sakollife.entity.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    // ── Injected from application.yml / environment variables ──────────────────

    /** EC P-256 public key X coordinate (base64url) from Supabase JWT settings */
    @Value("${supabase.jwt.public-key.x}")
    private String jwtPublicKeyX;

    /** EC P-256 public key Y coordinate (base64url) from Supabase JWT settings */
    @Value("${supabase.jwt.public-key.y}")
    private String jwtPublicKeyY;

    /** Key ID from Supabase JWT settings */
    @Value("${supabase.jwt.key-id}")
    private String jwtKeyId;

    /**
     * Expected issuer — must match Supabase token exactly.
     * Format: https://<project-ref>.supabase.co/auth/v1
     */
    @Value("${supabase.jwt.issuer}")
    private String jwtIssuer;

    /** Comma-separated list of allowed CORS origins */
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // ── Role hierarchy ─────────────────────────────────────────────────────────

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

    // ── JWT decoder with full claim validation ─────────────────────────────────

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        ECKey ecKey = new ECKey.Builder(
                Curve.P_256,
                Base64URL.from(jwtPublicKeyX),
                Base64URL.from(jwtPublicKeyY))
                .keyID(jwtKeyId)
                .build();

        java.security.interfaces.ECPublicKey publicKey = ecKey.toECPublicKey();

        com.nimbusds.jose.proc.JWSKeySelector<com.nimbusds.jose.proc.SecurityContext> keySelector =
                new com.nimbusds.jose.proc.SingleKeyJWSKeySelector<>(
                        com.nimbusds.jose.JWSAlgorithm.ES256, publicKey);

        DefaultJWTProcessor<com.nimbusds.jose.proc.SecurityContext> jwtProcessor =
                new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);

        // ── Enforce issuer + expiry + required claims ──────────────────────────
        // DefaultJWTClaimsVerifier checks:
        //   • exp  — token must not be expired
        //   • nbf  — not-before if present
        //   • iss  — must equal jwtIssuer
        // exactMatchClaims: iss must match exactly
        // requiredClaims:   sub must be present
        var exactMatch = new com.nimbusds.jwt.JWTClaimsSet.Builder()
                .issuer(jwtIssuer)
                .build();

        jwtProcessor.setJWTClaimsSetVerifier(
                new DefaultJWTClaimsVerifier<>(
                        exactMatch,
                        Set.of("sub", "exp") // required claims
                )
        );

        return new NimbusJwtDecoder(jwtProcessor);
    }

    // ── Security filter chain ──────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public — no identity required
                        .requestMatchers(HttpMethod.GET,  "/api/v1/quiz/questions").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/quiz/submit").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/majors/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/universities/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Guest-or-authenticated — controller handles identity internally
                        .requestMatchers("/api/v1/selected-major/**").permitAll()

                        // Admin only
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Everything else requires a valid JWT
                        .anyRequest().authenticated()
                )
                // Our filter runs before Spring's built-in UsernamePasswordAuthenticationFilter.
                // It sets the Authentication in the SecurityContext when a valid JWT is present,
                // and writes a 401 immediately when a token IS present but invalid/expired.
                .addFilterBefore(
                        new SupabaseJwtFilter(jwtDecoder),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ── CORS ───────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Whitelist only the headers the app actually uses
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Guest-Session-Id"
        ));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    // ── JWT filter ─────────────────────────────────────────────────────────────

    @Slf4j
    static class SupabaseJwtFilter extends OncePerRequestFilter {

        private final JwtDecoder jwtDecoder;

        SupabaseJwtFilter(JwtDecoder jwtDecoder) {
            this.jwtDecoder = jwtDecoder;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String userId = jwt.getSubject();

                    if (userId != null) {
                        String appRole = extractAppRole(jwt);
                        Collection<GrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + appRole)
                        );
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        UUID.fromString(userId), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated user {} with role {}", userId, appRole);
                    }

                } catch (Exception e) {
                    // Token was present but invalid or expired — reject immediately.
                    // Do NOT fall through: a bad token must never silently become a guest.
                    log.warn("JWT validation failed for {}: {}", request.getRequestURI(), e.getMessage());
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\":\"Invalid or expired token. Please sign in again.\"}"
                    );
                    return; // stop the filter chain
                }
            }

            filterChain.doFilter(request, response);
        }

        /**
         * Reads app_role from the JWT's app_metadata claim.
         * Supabase injects app_metadata automatically.
         * Defaults to USER if the claim is absent (e.g. new Google OAuth sign-ups).
         */
        @SuppressWarnings("unchecked")
        private String extractAppRole(Jwt jwt) {
            try {
                Object appMetadata = jwt.getClaim("app_metadata");
                if (appMetadata instanceof Map<?, ?> meta) {
                    Object role = meta.get("app_role");
                    if (role != null) {
                        return role.toString().toUpperCase();
                    }
                }
            } catch (Exception e) {
                log.debug("Could not extract app_role from JWT: {}", e.getMessage());
            }
            return Role.USER.name();
        }
    }
}