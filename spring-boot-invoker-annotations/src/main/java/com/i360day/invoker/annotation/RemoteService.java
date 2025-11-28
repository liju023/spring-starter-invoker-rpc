package com.i360day.invoker.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * 一个暴露类下有多个service实现的情况
 *
 * @author liju.z
 *
 **/
@Inherited
@Component
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE})
public @interface RemoteService {

}
