package tech.chhsich.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Builds and returns the application's SecurityFilterChain.
     *
     * Configures CSRF protection using a cookie-backed repository with the request attribute name "_csrf" and
     * excludes CSRF checks for public endpoints such as user registration/login, menu/categories, and API docs.
     * Disables Spring Security's CORS configuration to rely on WebConfig's global CORS settings, enforces stateless (JWT) session management,
     * and defines route-based authorization:
     * - Public: Swagger/API docs, /api/user/register, /api/user/login, /api/menu/**, /api/categories/**
     * - Authenticated: /api/orders/** and all other unspecified routes
     * - Admin-only: /api/admin/**
     * Disables form login and HTTP Basic authentication, and applies security headers (XSS block, CSP "default-src 'self'", same-origin frame options).
     *
     * @return the configured SecurityFilterChain
     * @throws Exception if configuring the HttpSecurity object fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 创建CSRF token处理器
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                // 启用CSRF保护，使用Cookie-based存储
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        // 对公开API豁免CSRF验证
                        .ignoringRequestMatchers("/api/user/register", "/api/user/login")
                        .ignoringRequestMatchers("/api/menu/**", "/api/categories/**")
                        .ignoringRequestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                )
                // 配置CORS - 使用WebConfig中的全局CORS配置
                .cors(cors -> cors.disable())
                // 设置无状态会话管理（JWT认证）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
                // 禁用basic认证和form登录，使用JWT
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                // 添加安全headers
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(cps -> cps.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

  
    /**
     * Provides a BCrypt-based PasswordEncoder for hashing and verifying user passwords.
     *
     * @return a PasswordEncoder implementation using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}