package tech.chhsich.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigurationIntegrationTest类
 * 验证所有配置类的集成和协同工作
 *
 * 测试场景：
 * 1. 验证所有配置类正常加载
 * 2. 验证CORS和安全配置不冲突
 * 3. 验证所有bean正常注入
 * 4. 验证配置间的依赖关系正确
 */
@SpringBootTest
class ConfigurationIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebConfig webConfig;

    @Autowired
    private tech.chhsich.backend.config.SecurityConfig securityConfig;

    @Test
    void testAllConfigurationsLoad() {
        // 测试场景1：验证所有配置类正常加载
        assertNotNull(webConfig, "WebConfig应该成功加载");
        assertNotNull(securityConfig, "SecurityConfig应该成功加载");

        // 验证配置类不为null且是正确的类型
        assertEquals(WebConfig.class, webConfig.getClass().getSuperclass(),
            "WebConfig应该是正确的类型");
        assertEquals(tech.chhsich.backend.config.SecurityConfig.class, securityConfig.getClass().getSuperclass(),
            "SecurityConfig应该是正确的类型");
    }

    @Test
    void testAllBeansExist() {
        // 测试场景2：验证所有必要的bean都存在
        // CORS配置bean - 通过名称获取我们的corsConfigurer
        WebMvcConfigurer corsConfigurer = applicationContext.getBean("corsConfigurer", WebMvcConfigurer.class);
        assertNotNull(corsConfigurer, "corsConfigurer bean应该存在");

        // 安全配置bean
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        assertNotNull(passwordEncoder, "PasswordEncoder bean应该存在");

        SecurityFilterChain securityFilterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(securityFilterChain, "SecurityFilterChain bean应该存在");
    }

    @Test
    void testConfigurationsDoNotConflict() {
        // 测试场景3：验证CORS和安全配置不冲突
        // 同时获取CORS和安全配置应该不会冲突
        WebMvcConfigurer corsConfigurer = applicationContext.getBean("corsConfigurer", WebMvcConfigurer.class);
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        SecurityFilterChain securityFilterChain = applicationContext.getBean(SecurityFilterChain.class);

        assertNotNull(corsConfigurer, "CORS配置应该可用");
        assertNotNull(passwordEncoder, "安全配置应该可用");
        assertNotNull(securityFilterChain, "安全过滤器链应该可用");

        // 验证bean是不同的实例，没有冲突
        assertNotEquals(corsConfigurer, passwordEncoder, "CORS和安全配置应该是不同的bean");
        assertNotEquals(passwordEncoder, securityFilterChain, "密码编码器和过滤器链应该是不同的bean");
    }

    @Test
    void testConfigurationIntegration() {
        // 测试场景4：验证配置间的集成工作正常
        // 获取所有配置相关的bean
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        boolean hasWebConfig = false;
        boolean hasSecurityConfig = false;
        boolean hasCorsConfigurer = false;
        boolean hasPasswordEncoder = false;
        boolean hasSecurityFilterChain = false;

        for (String beanName : allBeanNames) {
            if (beanName.contains("webConfig")) {
                hasWebConfig = true;
            }
            if (beanName.contains("securityConfig")) {
                hasSecurityConfig = true;
            }
            if (beanName.contains("corsConfigurer")) {
                hasCorsConfigurer = true;
            }
            if (beanName.contains("passwordEncoder")) {
                hasPasswordEncoder = true;
            }
            if (beanName.contains("securityFilterChain")) {
                hasSecurityFilterChain = true;
            }
        }

        // 验证所有必要的配置bean都存在
        assertTrue(hasWebConfig, "应该有WebConfig相关的bean");
        assertTrue(hasSecurityConfig, "应该有SecurityConfig相关的bean");
        assertTrue(hasCorsConfigurer, "应该有CORS配置相关的bean");
        assertTrue(hasPasswordEncoder, "应该有密码编码器相关的bean");
        assertTrue(hasSecurityFilterChain, "应该有安全过滤器链相关的bean");
    }

    @Test
    void testContextLoadsSuccessfully() {
        // 测试场景5：验证Spring Context成功加载
        // 这个测试确保整个应用上下文能够正常启动，没有配置冲突
        assertDoesNotThrow(() -> {
            applicationContext.getBeansOfType(Object.class);
        }, "Spring Context应该成功加载，没有配置冲突");

        // 验证上下文包含预期的bean数量
        int beanCount = applicationContext.getBeanDefinitionCount();
        assertTrue(beanCount > 50, "应用上下文应该包含足够数量的bean (实际: " + beanCount + ")");
    }

    @Test
    void testSpecificConfigurationsWorkTogether() {
        // 测试场景6：验证特定配置的协同工作
        // 验证CORS配置器可以正常工作
        WebMvcConfigurer corsConfigurer = applicationContext.getBean("corsConfigurer", WebMvcConfigurer.class);
        assertNotNull(corsConfigurer, "CORS配置器应该可以获取");

        // 验证安全配置可以正常工作
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        assertNotNull(passwordEncoder, "密码编码器应该可以获取");

        // 验证基本功能正常
        String encodedPassword = passwordEncoder.encode("test123");
        assertTrue(passwordEncoder.matches("test123", encodedPassword), "密码编码器应该正常工作");

        // 验证过滤器链存在
        SecurityFilterChain securityFilterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(securityFilterChain, "安全过滤器链应该存在");
        assertTrue(securityFilterChain.getFilters().size() > 0, "安全过滤器链应该包含过滤器");
    }
}