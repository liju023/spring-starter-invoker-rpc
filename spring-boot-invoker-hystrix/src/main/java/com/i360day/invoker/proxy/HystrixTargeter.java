package com.i360day.invoker.proxy;

import com.i360day.invoker.hystrix.FallbackFactory;
import com.i360day.invoker.hystrix.HystrixTargeterHandler;
import com.i360day.invoker.hystrix.SetterFactory;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author liju.z
 */
class HystrixTargeter implements Targeter {
    private SetterFactory setterFactory;

    public HystrixTargeter(SetterFactory setterFactory) {
        this.setterFactory = setterFactory;
    }

    @Override
    public <T> MethodInterceptor target(Map<Method, MethodInterceptor> dispatchMap, FallbackFactory fallbackFactory, TargetProxy<T> target) {
        return new HystrixTargeterHandler(setterFactory, fallbackFactory, dispatchMap, target);
    }
}
