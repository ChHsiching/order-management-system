package tech.chhsich.backend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tech.chhsich.backend.annotation.RequirePermission;
import tech.chhsich.backend.annotation.RequireRole;
import tech.chhsich.backend.exception.AuthorizationException;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限管理切面
 * 实现方法级别的权限控制
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Aspect
@Component
public class PermissionAspect {

    /**
     * 权限验证环绕通知
     */
    @Around("@annotation(tech.chhsich.backend.annotation.RequirePermission)")
    public Object aroundPermissionAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取权限注解
        RequirePermission permissionAnnotation = method.getAnnotation(RequirePermission.class);
        String requiredPermission = permissionAnnotation.value();
        boolean requireAll = permissionAnnotation.requireAll();

        // 3. 获取当前用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationException("用户未登录");
        }

        // 4. 验证权限
        if (!hasPermission(authentication, requiredPermission, requireAll)) {
            throw new AuthorizationException("权限不足，需要权限: " + requiredPermission);
        }

        // 5. 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 角色验证环绕通知
     */
    @Around("@annotation(tech.chhsich.backend.annotation.RequireRole)")
    public Object aroundRoleAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取角色注解
        RequireRole roleAnnotation = method.getAnnotation(RequireRole.class);
        int[] requiredRoles = roleAnnotation.value();
        boolean requireAll = roleAnnotation.requireAll();

        // 3. 获取当前用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationException("用户未登录");
        }

        // 4. 验证角色
        if (!hasRole(authentication, requiredRoles, requireAll)) {
            throw new AuthorizationException("角色权限不足，需要角色: " + Arrays.toString(requiredRoles));
        }

        // 5. 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 检查用户是否有指定权限
     *
     * @param authentication 用户认证信息
     * @param requiredPermission 需要的权限
     * @param requireAll 是否需要所有权限
     * @return 是否有权限
     */
    private boolean hasPermission(Authentication authentication, String requiredPermission, boolean requireAll) {
        // 这里可以根据实际业务逻辑从数据库或其他地方获取用户权限
        // 目前先使用Spring Security的authority机制
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + requiredPermission));
    }

    /**
     * 检查用户是否有指定角色
     *
     * @param authentication 用户认证信息
     * @param requiredRoles 需要的角色数组
     * @param requireAll 是否需要所有角色
     * @return 是否有角色
     */
    private boolean hasRole(Authentication authentication, int[] requiredRoles, boolean requireAll) {
        // 从认证信息中获取用户角色
        Object principal = authentication.getPrincipal();

        // 这里假设用户角色存储在认证信息的details中
        // 实际项目中可能需要从数据库查询用户角色
        Integer userRole = extractUserRole(principal);

        if (userRole == null) {
            return false;
        }

        if (requireAll) {
            // 需要所有角色（AND逻辑）
            return Arrays.stream(requiredRoles).allMatch(role -> role == userRole);
        } else {
            // 需要任一角色（OR逻辑）
            return Arrays.stream(requiredRoles).anyMatch(role -> role == userRole);
        }
    }

    /**
     * 从用户主体中提取角色信息
     *
     * @param principal 用户主体
     * @return 用户角色
     */
    private Integer extractUserRole(Object principal) {
        try {
            // 这里假设用户主体是一个包含角色信息的对象
            // 实际项目中需要根据具体的用户主体实现来提取角色
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 可以从数据库或其他地方获取用户角色
                // 这里先返回默认值，实际项目中需要修改
                return 1; // 假设默认为管理员角色
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}