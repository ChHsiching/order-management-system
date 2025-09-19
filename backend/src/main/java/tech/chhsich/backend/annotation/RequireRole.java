package tech.chhsich.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色要求注解
 * 用于方法级别的角色控制
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 需要的角色值
     * 0: 会员, 1: 管理员, 2: 接单员
     */
    int[] value();

    /**
     * 角色描述
     */
    String description() default "";

    /**
     * 是否需要所有角色（AND逻辑）
     * 如果为false，则满足任一角色即可（OR逻辑）
     */
    boolean requireAll() default false;
}