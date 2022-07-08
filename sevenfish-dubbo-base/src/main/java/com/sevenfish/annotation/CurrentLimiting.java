package com.sevenfish.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface CurrentLimiting {

    String limitKey() default "";

    int limitNum() default Integer.MAX_VALUE;

    int windowRange() default 1;

    int expireTime() default 3600;
}
