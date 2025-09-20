package tech.chhsich.backend.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.chhsich.backend.utils.JwtUtil;
import tech.chhsich.backend.exception.AuthenticationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * JWT认证过滤器
 * 拦截所有需要认证的请求，验证JWT token的有效性
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setUserDetailsService(@Lazy UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. 从请求头中获取token
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtil.extractTokenFromHeader(authHeader);

            // 2. 如果没有token，直接放行（让Spring Security处理）
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 验证token有效性
            if (!jwtUtil.validateToken(token)) {
                throw new AuthenticationException("无效的JWT token");
            }

            // 4. 检查token是否过期
            if (jwtUtil.isTokenExpired(token)) {
                throw new AuthenticationException("JWT token已过期");
            }

            // 5. 从token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                throw new AuthenticationException("无法从token中获取用户信息");
            }

            // 6. 如果用户还未认证，则进行认证
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从token中获取角色信息
                Map<String, Object> claims = jwtUtil.getClaimsFromToken(token);
                Integer role = claims != null && claims.containsKey("role") ? (Integer) claims.get("role") : null;

                // 创建自定义的UserDetails，包含角色信息
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password("") // JWT认证不需要密码
                    .authorities(role != null && role == 1 ? "ROLE_ADMIN" : "ROLE_USER")
                    .build();

                // 创建认证令牌
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 设置认证信息到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            // 7. 继续过滤器链
            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            // 处理认证异常
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"code\":401,\"message\":\"" + e.getMessage() + "\",\"data\":null}"
            );
        } catch (JWTVerificationException e) {
            // 处理JWT验证异常
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"code\":401,\"message\":\"JWT token验证失败\",\"data\":null}"
            );
        } catch (Exception e) {
            // 处理其他异常
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"code\":500,\"message\":\"服务器内部错误\",\"data\":null}"
            );
        }
    }

    /**
     * 判断是否应该过滤该请求
     * 跳过公开接口的JWT验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Debug logging
        System.out.println("[JWT Filter] Path: " + path);
        System.out.println("[JWT Filter] ContextPath: " + contextPath);

        // 处理上下文路径，如果contextPath为空，则使用空字符串
        String apiPrefix = contextPath.isEmpty() ? "/api" : contextPath + "/api";

        System.out.println("[JWT Filter] API Prefix: " + apiPrefix);

        // 检查是否是登录路径
        boolean isLoginPath = path.startsWith(apiPrefix + "/user/login");
        System.out.println("[JWT Filter] Is login path: " + isLoginPath);

        // 不需要验证的公开路径（处理上下文路径）
        boolean shouldNotFilter = path.startsWith(apiPrefix + "/user/register") ||
               path.startsWith(apiPrefix + "/user/login") ||
               path.startsWith(apiPrefix + "/admin/login") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.startsWith("/actuator") ||
               path.equals(apiPrefix + "/menu") ||
               path.equals(apiPrefix + "/categories") ||
               path.startsWith(apiPrefix + "/menu/") ||
               path.startsWith(apiPrefix + "/categories/") ||
               path.startsWith(apiPrefix + "/frontend/") ||
               path.startsWith(apiPrefix + "/cart/");

        System.out.println("[JWT Filter] Should not filter: " + shouldNotFilter);

        return shouldNotFilter;
    }
}