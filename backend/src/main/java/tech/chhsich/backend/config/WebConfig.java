package tech.chhsich.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    /**
     * Registers a WebMvcConfigurer bean that applies global CORS configuration.
     *
     * The returned configurer enables CORS for all request paths ("/**"), permits
     * origins "http://localhost:3000" and "http://localhost:8080", allows methods
     * GET, POST, PUT, DELETE and OPTIONS, accepts all request headers, allows
     * credentials, and sets preflight cache duration to 3600 seconds.
     *
     * @return a WebMvcConfigurer that applies the described global CORS settings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // 修改为匹配所有路径
                        .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}