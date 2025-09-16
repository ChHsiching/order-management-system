package tech.chhsich.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebConfig测试类
 * 验证WebConfig的CORS配置是否正确加载和配置
 *
 * 测试场景：
 * 1. WebConfig正常加载
 * 2. CORS配置器bean存在
 * 3. CORS配置方法存在并可调用
 * 4. CORS配置参数符合安全要求
 */
@SpringBootTest
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testWebConfigLoads() {
        // 测试场景1：WebConfig能正常加载
        assertNotNull(webConfig, "WebConfig应该成功加载");
    }

    @Test
    void testCorsConfigurerBeanExists() {
        // 测试场景2：CORS配置器bean存在
        String[] webMvcConfigurerBeans = applicationContext.getBeanNamesForType(WebMvcConfigurer.class);
        assertTrue(webMvcConfigurerBeans.length > 0, "应该存在WebMvcConfigurer bean用于CORS配置");
    }

    @Test
    void testCorsConfigurerMethodExists() {
        // 测试场景3：CORS配置方法存在并可调用
        try {
            Method corsConfigurerMethod = webConfig.getClass().getMethod("corsConfigurer");
            assertNotNull(corsConfigurerMethod, "corsConfigurer方法应该存在");

            // 调用方法获取WebMvcConfigurer
            WebMvcConfigurer configurer = (WebMvcConfigurer) corsConfigurerMethod.invoke(webConfig);
            assertNotNull(configurer, "corsConfigurer方法应该返回有效的WebMvcConfigurer");
        } catch (Exception e) {
            fail("WebConfig应该有可调用的corsConfigurer方法: " + e.getMessage());
        }
    }

    @Test
    void testCorsConfigurationParameters() {
        // 测试场景4：CORS配置参数符合安全要求
        try {
            // 获取WebMvcConfigurer实例
            Method corsConfigurerMethod = webConfig.getClass().getMethod("corsConfigurer");
            WebMvcConfigurer configurer = (WebMvcConfigurer) corsConfigurerMethod.invoke(webConfig);

            // 使用反射测试CORS配置
            TestCorsRegistry registry = new TestCorsRegistry();
            configurer.addCorsMappings(registry);

            // 验证CORS配置
            assertTrue(registry.isConfigured(), "CORS应该被正确配置");
            assertEquals("/**", registry.getMappedPath(), "CORS应该映射所有路径");
            assertTrue(registry.getAllowedOrigins().contains("http://localhost:3000"),
                "应该允许localhost:3000 Origin");
            assertTrue(registry.getAllowedOrigins().contains("http://localhost:8080"),
                "应该允许localhost:8080 Origin");
            assertFalse(registry.getAllowedOrigins().contains("*"),
                "不应该使用不安全的通配符Origin");
            assertTrue(registry.isAllowCredentials(), "应该允许凭证");
            assertEquals(3600, registry.getMaxAge(), "预检请求缓存时间应该为3600秒");
        } catch (Exception e) {
            fail("CORS配置参数验证失败: " + e.getMessage());
        }
    }

    /**
     * 测试用的CORS注册表实现
     */
    private static class TestCorsRegistry extends org.springframework.web.servlet.config.annotation.CorsRegistry {
        private String mappedPath;
        private java.util.Set<String> allowedOrigins = new java.util.HashSet<>();
        private boolean allowCredentials;
        private int maxAge;
        private boolean configured = false;

        @Override
        public CorsRegistration addMapping(String pathPattern) {
            this.mappedPath = pathPattern;
            this.configured = true;
            return new TestCorsRegistration(pathPattern);
        }

        public String getMappedPath() {
            return mappedPath;
        }

        public java.util.Set<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public int getMaxAge() {
            return maxAge;
        }

        public boolean isConfigured() {
            return configured;
        }

        private class TestCorsRegistration extends CorsRegistration {
            public TestCorsRegistration(String pathPattern) {
                super(pathPattern);
            }

            @Override
            public CorsRegistration allowedOrigins(String... origins) {
                for (String origin : origins) {
                    allowedOrigins.add(origin);
                }
                return this;
            }

            @Override
            public CorsRegistration allowCredentials(boolean allowCredentials) {
                TestCorsRegistry.this.allowCredentials = allowCredentials;
                return this;
            }

            @Override
            public CorsRegistration maxAge(long maxAge) {
                TestCorsRegistry.this.maxAge = (int) maxAge;
                return this;
            }
        }
    }
}