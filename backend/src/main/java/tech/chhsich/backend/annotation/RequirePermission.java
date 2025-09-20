package tech.chhsich.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限要求注解
 * 用于方法级别的权限控制
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限值
     */
    String value();

    /**
     * 权限描述
     */
    String description() default "";

    /**
     * 是否需要所有权限（AND逻辑）
     * 如果为false，则满足任一权限即可（OR逻辑）
     */
    boolean requireAll() default false;
}