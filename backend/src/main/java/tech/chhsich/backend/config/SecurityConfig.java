package tech.chhsich.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Spring Security配置类
 * 配置JWT认证和权限控制
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  
    /**
     * 构建并返回应用的SecurityFilterChain
     *
     * 配置JWT认证、CSRF保护、会话管理和路由授权：
     * - 公开接口：Swagger文档、用户注册登录、菜单分类等
     * - 需要认证的接口：订单、购物车等
     * - 管理员接口：需要ADMIN角色
     *
     * @param http HttpSecurity配置对象
     * @return 配置好的SecurityFilterChain
     * @throws Exception 配置失败时抛出异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 配置CSRF保护 - 为JWT API端点豁免CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**",
                                "/admin/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/swagger-config/**"
                        )
                )

                // 启用CORS，使用全局CORS配置
                .cors(cors -> {})

                // 设置无状态会话管理（JWT认证）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求授权
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

                        // 放行actuator健康检查端点
                        .requestMatchers("/actuator/**").permitAll()

                        // 公开接口 - 登录注册
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/info").permitAll()

                        // 公开接口 - 菜单相关
                        .requestMatchers("/api/menu/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/frontend/**").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()
                        // 由于有context-path，也需要匹配完整路径
                        .requestMatchers("/WebOrderSystem/api/menu/**").permitAll()
                        .requestMatchers("/WebOrderSystem/api/categories/**").permitAll()
                        .requestMatchers("/WebOrderSystem/api/frontend/**").permitAll()
                        .requestMatchers("/WebOrderSystem/api/cart/**").permitAll()

                        // 需要认证的接口
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/WebOrderSystem/api/orders/**").authenticated()

                        // 用户接口，除了登录注册外都需要认证
                        .requestMatchers("/api/user/me").authenticated()
                        .requestMatchers("/WebOrderSystem/api/user/me").authenticated()
                        .requestMatchers("/api/user/update").authenticated()
                        .requestMatchers("/WebOrderSystem/api/user/update").authenticated()
                        .requestMatchers("/api/user/change-password").authenticated()
                        .requestMatchers("/WebOrderSystem/api/user/change-password").authenticated()


                        // 管理员接口需要ADMIN角色
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 禁用表单登录和HTTP Basic认证
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

    }