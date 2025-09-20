package tech.chhsich.backend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志切面
 * 记录API请求和用户操作日志
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest request;

    /**
     * 定义切点：拦截所有Controller方法
     */
    @Pointcut("execution(* tech.chhsich.backend.controller..*.*(..))")
    public void controllerPointcut() {}

    /**
     * 定义切点：拦截需要记录操作日志的方法
     */
    @Pointcut("@annotation(tech.chhsich.backend.annotation.RequirePermission) || " +
            "@annotation(tech.chhsich.backend.annotation.RequireRole)")
    public void operationLogPointcut() {}

    /**
     * 控制器方法日志环绕通知
     */
    @Around("controllerPointcut()")
    public Object aroundControllerAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            // 记录请求开始
            logRequestStart(joinPoint);

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录请求结束
            logRequestEnd(joinPoint, result, startTime, null);

            return result;
        } catch (Exception e) {
            // 记录异常
            logRequestEnd(joinPoint, null, startTime, e);
            throw e;
        }
    }

    /**
     * 操作日志环绕通知
     */
    @Around("operationLogPointcut()")
    public Object aroundOperationLogAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录操作日志
            logOperation(joinPoint, result, startTime, null);

            return result;
        } catch (Exception e) {
            // 记录异常操作日志
            logOperation(joinPoint, null, startTime, e);
            throw e;
        }
    }

    /**
     * 记录请求开始日志
     */
    private void logRequestStart(ProceedingJoinPoint joinPoint) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            // 获取当前用户信息
            String username = getCurrentUsername();
            String userRole = getCurrentUserRole();

            // 获取请求信息
            String requestUri = request.getRequestURI();
            String method = request.getMethod();
            String ip = getClientIp();

            logger.info("API请求开始 - 用户: {}, 角色: {}, URI: {}, 方法: {}, IP: {}, 控制器: {}, 方法名: {}, 参数: {}",
                    username, userRole, requestUri, method, ip, className, methodName,
                    args.length > 0 ? Arrays.toString(args) : "无参数");

        } catch (Exception e) {
            logger.error("记录请求开始日志失败", e);
        }
    }

    /**
     * 记录请求结束日志
     */
    private void logRequestEnd(ProceedingJoinPoint joinPoint, Object result, long startTime, Exception exception) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            // 获取当前用户信息
            String username = getCurrentUsername();
            String userRole = getCurrentUserRole();

            // 获取请求信息
            String requestUri = request.getRequestURI();
            String method = request.getMethod();
            String ip = getClientIp();

            long executionTime = System.currentTimeMillis() - startTime;

            if (exception != null) {
                logger.error("API请求失败 - 用户: {}, 角色: {}, URI: {}, 方法: {}, IP: {}, 控制器: {}, 方法名: {}, 执行时间: {}ms, 异常: {}",
                        username, userRole, requestUri, method, ip, className, methodName, executionTime, exception.getMessage());
            } else {
                logger.info("API请求成功 - 用户: {}, 角色: {}, URI: {}, 方法: {}, IP: {}, 控制器: {}, 方法名: {}, 执行时间: {}ms",
                        username, userRole, requestUri, method, ip, className, methodName, executionTime);
            }

        } catch (Exception e) {
            logger.error("记录请求结束日志失败", e);
        }
    }

    /**
     * 记录操作日志
     */
    private void logOperation(ProceedingJoinPoint joinPoint, Object result, long startTime, Exception exception) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            // 获取当前用户信息
            String username = getCurrentUsername();
            String userRole = getCurrentUserRole();

            // 获取请求信息
            String requestUri = request.getRequestURI();
            String ip = getClientIp();

            long executionTime = System.currentTimeMillis() - startTime;

            // 构造操作日志信息
            Map<String, Object> operationLog = new HashMap<>();
            operationLog.put("username", username);
            operationLog.put("role", userRole);
            operationLog.put("operationType", className + "." + methodName);
            operationLog.put("description", "用户操作");
            operationLog.put("method", requestUri);
            operationLog.put("params", args.length > 0 ? Arrays.toString(args) : "无参数");
            operationLog.put("result", result != null ? "成功" : "失败");
            operationLog.put("executionTime", executionTime);
            operationLog.put("ip", ip);
            operationLog.put("status", exception == null ? 1 : 0);
            operationLog.put("errorMessage", exception != null ? exception.getMessage() : "");
            operationLog.put("createTime", LocalDateTime.now());

            if (exception != null) {
                logger.warn("用户操作异常 - 操作日志: {}", objectMapper.writeValueAsString(operationLog), exception);
            } else {
                logger.info("用户操作成功 - 操作日志: {}", objectMapper.writeValueAsString(operationLog));
            }

        } catch (Exception e) {
            logger.error("记录操作日志失败", e);
        }
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            logger.debug("获取当前用户名失败", e);
        }
        return "匿名用户";
    }

    /**
     * 获取当前用户角色
     */
    private String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getAuthorities().toString();
            }
        } catch (Exception e) {
            logger.debug("获取当前用户角色失败", e);
        }
        return "无角色";
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        try {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            String xRealIp = request.getHeader("X-Real-IP");
            String remoteAddr = request.getRemoteAddr();

            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }

            if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                return xRealIp.trim();
            }

            return remoteAddr;
        } catch (Exception e) {
            logger.debug("获取客户端IP失败", e);
            return "未知IP";
        }
    }
}