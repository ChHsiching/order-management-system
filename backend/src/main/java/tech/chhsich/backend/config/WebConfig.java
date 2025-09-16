package tech.chhsich.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    /**
     * Creates a WebMvcConfigurer bean that configures global CORS settings.
     *
     * <p>The returned configurer applies to all paths ("/**"), allows requests from
     * http://localhost:3000 and http://localhost:8080, permits GET, POST, PUT, DELETE and OPTIONS,
     * accepts all headers, enables credentials, and sets preflight cache duration to 3600 seconds.
     *
     * @return a WebMvcConfigurer that applies the described CORS configuration
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