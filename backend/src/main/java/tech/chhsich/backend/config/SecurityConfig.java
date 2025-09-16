package tech.chhsich.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures and returns the application's SecurityFilterChain.
     *
     * <p>Disables CSRF, form login, and HTTP Basic auth. Applies route-based access rules:
     * permits unauthenticated access to Swagger resources and public endpoints
     * (/api/user/register, /api/user/login, /api/menu/**, /api/categories/**),
     * requires authentication for /api/orders/** and all other unspecified routes,
     * and requires the ADMIN role for /api/admin/**.
     *
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // 放行Swagger相关路径
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/swagger-config/**"
                        ).permitAll()
                        // 公开接口
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll()
                        .requestMatchers("/api/menu/**", "/api/categories/**").permitAll()
                        // 需要认证的接口
                        .requestMatchers("/api/orders/**").authenticated()
                        // 管理员接口
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
    /**
     * Provides a BCrypt-based PasswordEncoder for hashing and verifying user passwords.
     *
     * <p>Returns a PasswordEncoder that uses the BCrypt algorithm with sensible defaults.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}