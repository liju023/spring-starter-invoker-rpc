package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteModule;
import com.i360day.invoker.common.InvokerUrlUtils;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.common.RemoteModuleUtils;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.hystrix.DefaultFallbackFactory;
import com.i360day.invoker.hystrix.DefaultHystrixMethodInterceptor;
import com.i360day.invoker.hystrix.FallbackFactory;
import com.i360day.invoker.proxy.DefaultTargeterHandler;
import com.i360day.invoker.proxy.TargetProxy;
import com.i360day.invoker.proxy.Targeter;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @program: spring-cloud-invoker-parent
 * @description:
 * @author: liju.z
 * @create: 2022-03-27 08:07
 **/
public class InvokerBuilder {
    /**
     * 代理目标接口类
     */
    private Class<?> interfaceClass;
    /**
     * ClassLoader
     */
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    /**
     * 代理目标接口类
     */
    private TargetProxy targetProxy;
    /**
     * 回调工厂
     */
    private Class<? extends FallbackFactory> fallbackFactory = DefaultFallbackFactory.class;
    /**
     * invoker 上下文
     */
    private InvokerContext invokerContext;
    /**
     * 执行 Hystrix Targeter
     */
    private Targeter targeter = new DefaultTargeterHandler();
    /**
     * 目标MethodInterceptor代理
     */
    private MethodInterceptor interceptor;
    /**
     * 回调目标类
     */
    private Class<?> fallback;

    private InvokerBuilder() {
    }

    public static InvokerBuilder create() {
        return new InvokerBuilder();
    }

    public Class<?> getInterfaceClass() {
        Assert.isTrue(ObjectUtils.isNotEmpty(interfaceClass), "interfaceClass is null");

        return interfaceClass;
    }

    public InvokerBuilder interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public InvokerBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public InvokerBuilder targetProxy(TargetProxy targetProxy) {
        this.targetProxy = targetProxy;
        return this;
    }

    public InvokerBuilder targeter(Targeter targeter) {
        Assert.notNull(targeter, "targeter Not found in spring context");

        this.targeter = targeter;
        return this;
    }

    public InvokerBuilder interceptor(MethodInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public InvokerBuilder interceptor(Function<InvokerBuilder, MethodInterceptor> customizer) {
        this.interceptor = customizer.apply(this);
        return this;
    }

    public InvokerBuilder fallbackFactory(Class<? extends FallbackFactory> fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
        return this;
    }

    public InvokerBuilder fallback(Class<?> fallback) {
        this.fallback = fallback;
        return this;
    }

    public InvokerBuilder httpInvokerContext(InvokerContext invokerContext) {
        this.invokerContext = invokerContext;
        return this;
    }

    public TargetProxy getTargetProxy() {

        //default attr
        if (this.targetProxy == null) {
            //not null
            Class<?> interfaceClass = getInterfaceClass();

            //set targetProxy
            Map<String, Object> remoteModuleToMap = RemoteModuleUtils.getRemoteModuleToMap(interfaceClass);
            URI[] uris = Arrays.stream((String[]) remoteModuleToMap.get("address")).map(address -> InvokerUrlUtils.getClientUrl(address, interfaceClass)).toArray(URI[]::new);
            this.targetProxy = new TargetProxy(interfaceClass, uris, (String) remoteModuleToMap.get("clientUser"), (String) remoteModuleToMap.get("clientPassword"), getClassLoader());
        }

        return targetProxy;
    }

    public Class<? extends FallbackFactory> getFallbackFactory() {
        return fallbackFactory;
    }

    public Targeter getTargeter() {
        return targeter;
    }

    public MethodInterceptor getInterceptor() {
        Assert.isTrue(ObjectUtils.isNotEmpty(interceptor), "interceptor MethodInterceptor is null");

        return interceptor;
    }

    public InvokerContext getHttpInvokerContext() {
        return invokerContext;
    }

    /**
     * fallback
     *
     * @return
     */
    public Class<?> getFallback() {
        return fallback;
    }

    /**
     * Method Interceptor
     *
     * @return
     */
    public MethodInterceptor getMethodInterceptor() {
        return getTargeter().target(getDispatchMap(), createFallbackFactory(), getTargetProxy());
    }

    /**
     * build
     *
     * @param <T>
     * @return
     */
    public <T> T build() {
        return (T) new ProxyFactory(getInterfaceClass(), getMethodInterceptor()).getProxy(getClassLoader());
    }

    /**
     * 将当前类的所有方法存储
     *
     * @return
     */
    private Map<Method, MethodInterceptor> getDispatchMap() {
        Map<Method, MethodInterceptor> dispatchMethodMap = new LinkedHashMap<>();
        Method[] methods = getInterfaceClass().getMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            dispatchMethodMap.put(method, getInterceptor());
        }
        return dispatchMethodMap;
    }

    /**
     * 创建降级工厂
     *
     * @return
     */
    private FallbackFactory createFallbackFactory() {

        if (getHttpInvokerContext() == null) {

            return new DefaultFallbackFactory();
        }

        //获取 FallbackFactory
        if (ObjectUtils.isNotEmpty(getFallback()) && !void.class.equals(getFallback())) {

            return new DefaultHystrixMethodInterceptor(getFallback(), getHttpInvokerContext());

        } else if (ObjectUtils.isNotEmpty(getFallbackFactory()) && !DefaultFallbackFactory.class.equals(getFallbackFactory())) {

            FallbackFactory fallbackFactory = getHttpInvokerContext().getBean(getFallbackFactory(), true);

            //如果获取不到，重新实例化一个
            if (fallbackFactory == null) {

                Assert.isTrue(!getFallbackFactory().isInterface(), getFallbackFactory().getName() + " can only be specified on an service");

                return getHttpInvokerContext().createBean(getFallbackFactory());
            }

            return fallbackFactory;
        }

        return new DefaultFallbackFactory();
    }
}
