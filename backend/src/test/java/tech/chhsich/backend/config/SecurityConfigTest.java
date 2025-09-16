package tech.chhsich.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityConfig测试类
 * 验证安全配置的正确性和安全性规则符合要求
 *
 * 测试场景：
 * 1. SecurityConfig正常加载
 * 2. 安全配置bean存在
 * 3. 密码编码器配置正确
 * 4. 安全过滤链配置存在
 */
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testSecurityConfigLoads() {
        // 测试场景1：SecurityConfig能正常加载
        assertDoesNotThrow(() -> {
            applicationContext.getBean(tech.chhsich.backend.config.SecurityConfig.class);
        }, "SecurityConfig应该成功加载");
    }

    @Test
    void testSecurityBeansExist() {
        // 测试场景2：安全配置bean存在
        assertNotNull(applicationContext.getBean(PasswordEncoder.class),
            "PasswordEncoder bean应该存在");
        assertNotNull(applicationContext.getBean(SecurityFilterChain.class),
            "SecurityFilterChain bean应该存在");
    }

    @Test
    void testPasswordEncoderFunctionality() {
        // 测试场景3：密码编码器配置正确
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);

        // 测试密码编码和解码
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "编码后的密码不应该为空");
        assertNotEquals(rawPassword, encodedPassword, "编码后的密码应该与原始密码不同");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "密码编码器应该正确匹配密码");
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword), "密码编码器应该拒绝错误密码");
    }

    @Test
    void testSecurityFilterChainConfiguration() {
        // 测试场景4：安全过滤链配置存在
        SecurityFilterChain securityFilterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(securityFilterChain, "SecurityFilterChain应该被正确配置");

        // 验证过滤器链的基本功能
        assertTrue(securityFilterChain.getFilters().size() > 0, "安全过滤器链应该包含过滤器");
    }
}