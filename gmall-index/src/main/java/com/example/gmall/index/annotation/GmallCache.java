package com.example.gmall.index.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存key的前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存过期时间
     * @return
     */
    int timeout() default 5;

    /**
     * 防止缓存雪崩指定的随机值范围，分钟
     * @return
     */
    int random() default 5;
}
