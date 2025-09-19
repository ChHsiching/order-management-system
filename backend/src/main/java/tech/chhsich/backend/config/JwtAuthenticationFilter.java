package tech.chhsich.backend.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
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
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

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

        // 移除上下文路径
        if (path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        // 不需要验证的公开路径
        return path.startsWith("/api/user/register") ||
               path.startsWith("/api/user/login") ||
               path.startsWith("/api/admin/login") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.startsWith("/actuator") ||
               path.equals("/api/menu") ||
               path.equals("/api/categories") ||
               path.startsWith("/api/menu/") ||
               path.startsWith("/api/categories/");
    }
}