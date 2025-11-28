package com.i360day.invoker.hystrix;

import com.i360day.invoker.context.InvokerContext;
import org.springframework.util.Assert;

/**
 * @description: 回调
 * @author: liju.z
 * @create: 2019-10-27 10:31
 **/
public class DefaultHystrixMethodInterceptor<T> implements FallbackFactory {

    private Class<?> fallbackClazz;
    private Object fallbackService;

    /**
     * default
     * @param fallbackClazz
     * @param invokerContext
     */
    public DefaultHystrixMethodInterceptor(Class<?> fallbackClazz, InvokerContext invokerContext) {
        Assert.isTrue(!fallbackClazz.isInterface(), fallbackClazz.getName() + " can only be specified on an service");

        this.fallbackClazz = fallbackClazz;
        this.fallbackService = invokerContext.getBean(fallbackClazz, true);
        // is null create bean
        if(this.fallbackService == null){
            invokerContext.createBean(this.fallbackClazz);
        }
    }

    @Override
    public Object create(Throwable throwable) {
        return fallbackService;
    }
}
