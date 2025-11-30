package com.mcw.distributed.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcConsumer {

    String version() default "1.0.0";
}
