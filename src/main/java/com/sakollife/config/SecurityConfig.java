package com.sakollife.config;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import com.sakollife.entity.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Value("${supabase.project.ref}")
    private String supabaseProjectRef;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        String x = "CW9wzr6970QYbDm1aoYEyz5seFQOwYtGocB2O18pC1U";
        String y = "pI7aAk1RqvHCMSwza4wZ8QaDIagME6NiTTu_K1s-xHk";

        ECKey ecKey = new ECKey.Builder(Curve.P_256, Base64URL.from(x), Base64URL.from(y))
                .keyID("8f5af4f0-ad39-4da9-af43-2c5c578a2a5c")
                .build();

        java.security.interfaces.ECPublicKey publicKey = ecKey.toECPublicKey();

        com.nimbusds.jose.crypto.ECDSAVerifier verifier =
                new com.nimbusds.jose.crypto.ECDSAVerifier(publicKey);

        com.nimbusds.jose.proc.JWSKeySelector<com.nimbusds.jose.proc.SecurityContext> keySelector =
                new com.nimbusds.jose.proc.SingleKeyJWSKeySelector<>(
                        com.nimbusds.jose.JWSAlgorithm.ES256, publicKey);

        com.nimbusds.jwt.proc.DefaultJWTProcessor<com.nimbusds.jose.proc.SecurityContext> jwtProcessor =
                new com.nimbusds.jwt.proc.DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);

        return new org.springframework.security.oauth2.jwt.NimbusJwtDecoder(jwtProcessor);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/quiz/submit").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/majors/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/universities/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/quiz/questions").permitAll()

                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(new SupabaseJwtFilter(jwtDecoder), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /**
     * Custom filter that:
     * 1. Validates the Supabase JWT using the JWKS endpoint (ES256)
     * 2. Extracts the user UUID from the 'sub' claim
     * 3. Reads the app role from 'app_metadata.app_role' (defaults to USER)
     * 4. Sets a UsernamePasswordAuthenticationToken with UUID as principal
     *
     * This gives controllers access to the UUID via:
     *   UUID userId = (UUID) authentication.getPrincipal();
     *
     * To make a user admin:
     *   Supabase Dashboard → Authentication → Users → select user → Edit App Metadata:
     *   { "app_role": "ADMIN" }
     *   Then the user must log in again to get a fresh token containing the new claim.
     */
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
                        log.debug("Authenticated user: {} with role: {}", userId, appRole);
                    }
                } catch (Exception e) {
                    log.warn("JWT validation failed: {}", e.getMessage());
                }
            }

            filterChain.doFilter(request, response);
        }

        /**
         * Reads app_role from the JWT's app_metadata claim.
         * Supabase puts custom app_metadata into the JWT automatically.
         *
         * Example app_metadata to set in Supabase Dashboard:
         *   { "app_role": "ADMIN" }
         *
         * Defaults to USER if not set.
         */
        @SuppressWarnings("unchecked")
        private String extractAppRole(Jwt jwt) {
            try {
                Object appMetadata = jwt.getClaim("app_metadata");
                if (appMetadata instanceof Map) {
                    Object role = ((Map<String, Object>) appMetadata).get("app_role");
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
