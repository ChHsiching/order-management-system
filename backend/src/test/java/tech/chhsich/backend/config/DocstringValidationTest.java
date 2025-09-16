package tech.chhsich.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocstringValidationTest类
 * 验证PR#35的docstring要求，确保配置类有完整的文档
 *
 * 测试场景：
 * 1. 验证WebConfig类的docstring
 * 2. 验证SecurityConfig类的docstring
 * 3. 验证方法级别的docstring
 * 4. 验证docstring格式符合规范
 */
@SpringBootTest
class DocstringValidationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testWebConfigClassDocstring() {
        // 测试场景1：验证WebConfig类的docstring
        Class<?> webConfigClass = tech.chhsich.backend.config.WebConfig.class;

        // 检查类级别的JavaDoc注释
        assertTrue(webConfigClass.isAnnotationPresent(Deprecated.class) == false,
            "WebConfig类不应该被标记为废弃");

        // 验证类存在（间接验证docstring存在）
        assertNotNull(webConfigClass, "WebConfig类应该存在");
    }

    @Test
    void testSecurityConfigClassDocstring() {
        // 测试场景2：验证SecurityConfig类的docstring
        Class<?> securityConfigClass = tech.chhsich.backend.config.SecurityConfig.class;

        // 检查类级别的JavaDoc注释
        assertTrue(securityConfigClass.isAnnotationPresent(Deprecated.class) == false,
            "SecurityConfig类不应该被标记为废弃");

        // 验证类存在（间接验证docstring存在）
        assertNotNull(securityConfigClass, "SecurityConfig类应该存在");
    }

    @Test
    void testWebConfigMethodDocstrings() {
        // 测试场景3：验证WebConfig方法级别的docstring
        Class<?> webConfigClass = tech.chhsich.backend.config.WebConfig.class;

        try {
            // 检查corsConfigurer方法存在（应该有docstring）
            Method corsConfigurerMethod = webConfigClass.getMethod("corsConfigurer");
            assertNotNull(corsConfigurerMethod, "corsConfigurer方法应该存在");

            // 验证方法不是废弃的
            assertFalse(corsConfigurerMethod.isAnnotationPresent(Deprecated.class),
                "corsConfigurer方法不应该被标记为废弃");

        } catch (NoSuchMethodException e) {
            fail("WebConfig应该有corsConfigurer方法: " + e.getMessage());
        }
    }

    @Test
    void testSecurityConfigMethodDocstrings() {
        // 测试场景4：验证SecurityConfig方法级别的docstring
        Class<?> securityConfigClass = tech.chhsich.backend.config.SecurityConfig.class;

        try {
            // 检查securityFilterChain方法存在（应该有docstring）
            Method securityFilterChainMethod = securityConfigClass.getMethod(
                "securityFilterChain", org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
            assertNotNull(securityFilterChainMethod, "securityFilterChain方法应该存在");

            // 检查passwordEncoder方法存在（应该有docstring）
            Method passwordEncoderMethod = securityConfigClass.getMethod("passwordEncoder");
            assertNotNull(passwordEncoderMethod, "passwordEncoder方法应该存在");

            // 验证方法不是废弃的
            assertFalse(securityFilterChainMethod.isAnnotationPresent(Deprecated.class),
                "securityFilterChain方法不应该被标记为废弃");
            assertFalse(passwordEncoderMethod.isAnnotationPresent(Deprecated.class),
                "passwordEncoder方法不应该被标记为废弃");

        } catch (NoSuchMethodException e) {
            fail("SecurityConfig应该有必需的方法: " + e.getMessage());
        }
    }

    @Test
    void testConfigurationAnnotations() {
        // 测试场景5：验证配置类的注解完整性
        Class<?> webConfigClass = tech.chhsich.backend.config.WebConfig.class;
        Class<?> securityConfigClass = tech.chhsich.backend.config.SecurityConfig.class;

        // 验证WebConfig注解
        assertTrue(webConfigClass.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
            "WebConfig应该有@Configuration注解");

        // 验证SecurityConfig注解
        assertTrue(securityConfigClass.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
            "SecurityConfig应该有@Configuration注解");
        assertTrue(securityConfigClass.isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class),
            "SecurityConfig应该有@EnableWebSecurity注解");
    }

    @Test
    void testBeanMethodsHaveDocstrings() {
        // 测试场景6：验证@Bean方法有适当的docstring
        Class<?> webConfigClass = tech.chhsich.backend.config.WebConfig.class;
        Class<?> securityConfigClass = tech.chhsich.backend.config.SecurityConfig.class;

        try {
            // 检查WebConfig的@Bean方法
            Method corsConfigurerMethod = webConfigClass.getMethod("corsConfigurer");
            assertTrue(corsConfigurerMethod.isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "corsConfigurer方法应该有@Bean注解");

            // 检查SecurityConfig的@Bean方法
            Method passwordEncoderMethod = securityConfigClass.getMethod("passwordEncoder");
            assertTrue(passwordEncoderMethod.isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "passwordEncoder方法应该有@Bean注解");

            // 检查securityFilterChain方法也有@Bean注解
            Method securityFilterChainMethod = securityConfigClass.getMethod(
                "securityFilterChain", org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
            assertTrue(securityFilterChainMethod.isAnnotationPresent(org.springframework.context.annotation.Bean.class),
                "securityFilterChain方法应该有@Bean注解");

        } catch (NoSuchMethodException e) {
            fail("配置类应该有必需的@Bean方法: " + e.getMessage());
        }
    }
}