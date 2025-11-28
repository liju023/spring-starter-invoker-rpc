package com.i360day.invoker.annotation;

import java.lang.annotation.*;


/**
 * 使用security时，可直接忽略该server默认放行
 * <p> @date: 2021/01/27 0027  15:19</p>
 *
 * @author liju.z
 *
 **/
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE})
public @interface RemoteIgnoreSecurity {

}
