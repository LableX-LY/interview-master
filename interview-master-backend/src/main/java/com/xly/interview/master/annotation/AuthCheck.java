package com.xly.interview.master.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/11 15:32
 * @description 自定义权限校验注解
 **/

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须具有某个角色
     */
    String mustRole() default "";

}
