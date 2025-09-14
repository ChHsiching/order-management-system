package tech.chhsich.backend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.chhsich.backend.pojo.ResponseMessage;
import tech.chhsich.backend.utils.JwUtil;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 获取请求url
        String url = request.getRequestURL().toString();
        
        // 判断请求url中是否包含login，如果包含，说明是登录操作，放行
        if (url.contains("login")) {
            return true;
        }

        // 获取请求头中的令牌（token）
        String jwt = request.getHeader("Authorization");
        
        // 判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(jwt) || !jwt.startsWith("Bearer ")) {
            ResponseMessage error = ResponseMessage.error("NOT_LOGIN");
            // 手动转换对象 -> json --------> 阿里巴巴 fastJSON
            String notLogin = new ObjectMapper().writeValueAsString(error);
            response.getWriter().write(notLogin);
            return false;
        }

        // 解析token，如果解析失败，返回错误结果（未登录）
        try {
            String token = jwt.substring(7); // 移除 "Bearer " 前缀
            Map<String, Object> claims = JwUtil.parseToken(token);
            
            // 将解析出的用户信息存储到ThreadLocal中，便于后续使用
            String username = (String) claims.get("username");
            Integer role = (Integer) claims.get("role");
            
            // 将用户信息存储到request中
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            
        } catch (Exception e) { // jwt解析失败
            ResponseMessage error = ResponseMessage.error("NOT_LOGIN");
            // 手动转换对象 -> json --------> 阿里巴巴 fastJSON
            String notLogin = new ObjectMapper().writeValueAsString(error);
            response.getWriter().write(notLogin);
            return false;
        }

        // 放行
        return true;
    }
}
