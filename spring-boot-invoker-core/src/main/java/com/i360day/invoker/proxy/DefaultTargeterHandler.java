package com.i360day.invoker.proxy;

import com.i360day.invoker.annotation.RemoteDisable;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.exception.RemoteClientHystrixException;
import com.i360day.invoker.exception.RemoteDisableException;
import com.i360day.invoker.hystrix.FallbackFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author liju.z
 */
public class DefaultTargeterHandler implements Targeter {

    private Logger logger = LoggerFactory.getLogger(DefaultTargeterHandler.class);

    @Override
    public <T> MethodInterceptor target(Map<Method, MethodInterceptor> dispatchMap, FallbackFactory fallbackFactory, TargetProxy<T> target) {
        return new DefaultInvocationHandler(fallbackFactory, dispatchMap, target);
    }

    private class DefaultInvocationHandler implements MethodInterceptor {
        private Map<Method, MethodInterceptor> dispatchMethodMap;
        private Map<Method, Method> fallbackMethodMap;
        private FallbackFactory fallbackFactory;
        private TargetProxy target;

        public DefaultInvocationHandler(FallbackFactory fallbackFactory, Map<Method, MethodInterceptor> dispatchMethodMap, TargetProxy target) {
            this.fallbackFactory = fallbackFactory;
            this.dispatchMethodMap = dispatchMethodMap;
            this.fallbackMethodMap = toFallbackMethodMap(dispatchMethodMap.keySet());
            this.target = target;
        }

        private Map<Method, Method> toFallbackMethodMap(Set<Method> methodInterceptors) {
            Map<Method, Method> fallbackMethodMap = new LinkedHashMap<>();
            for (Method method : methodInterceptors) {
                fallbackMethodMap.put(method, method);
            }
            return fallbackMethodMap;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Object proxy = invocation.getThis();
            Method method = invocation.getMethod();
            Object[] args = invocation.getArguments();
            Object toString = ObjectUtils.isToString(this, method, args);
            if(ObjectUtils.isNotEmpty(toString)) return toString;

            DefaultFallback defaultFallback = new DefaultFallback() {

                @Override
                public Object run() throws Throwable {
                    InvokerMethodInvocation methodInvocation = new InvokerMethodInvocation(proxy, method, args);

                    if(method.getAnnotation(RemoteDisable.class) != null) {
                        throw new RemoteDisableException(String.format("%s %s method is disabled", method.getDeclaringClass(), method.getName()));
                    }

                    return dispatchMethodMap.get(method).invoke(methodInvocation);
                }

                @Override
                public Object getFallback(Throwable throwable) {
                    try {
                        Object obj = Optional.ofNullable(fallbackFactory.create(throwable)).orElseThrow(() -> throwable);
                        return fallbackMethodMap.get(method).invoke(obj, args);
                    } catch (Throwable ex) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("降级处理错误：{}", ex);
                        }
                        throw new RemoteClientHystrixException(ex, ex.toString());
                    }
                }
            };
            return defaultFallback.execute();
        }

        @Override
        public String toString() {
            return target.toString();
        }

        @Override
        public int hashCode() {
            return target.hashCode();
        }
    }

    private abstract class DefaultFallback {

        public abstract Object run() throws Throwable;

        public abstract Object getFallback(Throwable throwable);

        public Object execute() {
            try {
                return run();
            } catch (Throwable throwable) {
                if (logger.isDebugEnabled()) {
                    logger.debug("执行回调错误");
                }
                return getFallback(throwable);
            }
        }
    }
}
