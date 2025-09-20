package tech.chhsich.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.chhsich.backend.service.UserService;

/**
 * 基础Bean配置类
 * 提供一些基础组件的配置
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Configuration
public class BeanConfig {

    private final UserService userService;

    public BeanConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    /**
     * 提供密码编码器
     *
     * @return PasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 提供基于数据库的UserDetailsService
     *
     * @return 使用数据库的用户详情服务
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            tech.chhsich.backend.entity.Administrator user = userService.getUserByUsername(username);
            if (user != null) {
                return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole() == 1 ? "ADMIN" : "USER")
                    .build();
            }
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("用户不存在: " + username);
        };
    }

    /**
     * 提供DaoAuthenticationProvider
     *
     * @return 配置了密码编码器和用户详情服务的认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 提供AuthenticationManager
     *
     * @return 配置了认证提供者的认证管理器
     * @throws Exception 配置失败时抛出异常
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new org.springframework.security.authentication.ProviderManager(authenticationProvider());
    }
}