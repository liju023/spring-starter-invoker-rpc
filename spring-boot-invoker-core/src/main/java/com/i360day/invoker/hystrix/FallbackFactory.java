package com.i360day.invoker.hystrix;

/**
 * @program: spring-cloud-invoker-parent
 * @description: 回调工厂
 * @author: liju.z
 * @create: 2019-10-27 10:33
 **/
public interface FallbackFactory<T> {

    /**
     *  @author liju.z 
     *
     *  @Description 创建异常回调 
     *
     *  @Date  13:23 
     *
     *  @Param [throwable] 
     *
     *  @return [throwable] 
     *
     **/
    T create(Throwable throwable);

}
