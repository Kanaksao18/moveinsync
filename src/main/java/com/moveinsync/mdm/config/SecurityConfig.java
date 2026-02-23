package com.moveinsync.mdm.config;

import com.moveinsync.mdm.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' http://localhost:5173 http://localhost:8088")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Public/system endpoints.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/device/heartbeat", "/api/auth/**", "/actuator/**").permitAll()
                        .requestMatchers("/api/version/latest").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Write operations with stricter role requirements.
                        .requestMatchers(HttpMethod.POST, "/api/version/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/device").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/compatibility/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/compatibility/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/compatibility/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/schedule").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/update/schedule").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rollout/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/schedule/*/approve").hasAnyRole("ADMIN", "PRODUCT_HEAD")
                        .requestMatchers(HttpMethod.POST, "/api/schedule/*/reject").hasAnyRole("ADMIN", "PRODUCT_HEAD")
                        .requestMatchers(HttpMethod.POST, "/api/device-update/state").hasAnyRole("ADMIN", "PRODUCT_HEAD")

                        // Read and non-destructive operations for all authorized roles.
                        .requestMatchers("/api/version/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/device/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/compatibility/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/schedule/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/rollout/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/device-update/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "VIEWER", "PRODUCT_HEAD")
                        .anyRequest().denyAll()
                );

        // JWT filter authenticates bearer token before Spring's username/password chain.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
