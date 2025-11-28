package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteClientEntity;
import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.executor.HttpComponentsInvokerRequestExecutor;
import com.i360day.invoker.proxy.TargetProxy;
import com.i360day.invoker.proxy.Targeter;
import com.i360day.invoker.support.RemoteInvocationFactory;
import com.i360day.invoker.support.UrlBasedRemoteAccessor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.Order;

/**
 * http 接口代理模式
 *
 * @author liju.z
 */
@Order(Integer.MIN_VALUE)
public class HttpInvokerClientFactoryBean extends UrlBasedRemoteAccessor implements FactoryBean<Object> {
    /**
     * 自定义上下文
     */
    private InvokerContext invokerContext;

    /**
     * 代理目标类信息
     */
    private TargetProxy targetProxy;

    /**
     * remoteClient详细
     */
    private RemoteClientEntity remoteClient;

    /**
     * 构造接口代理
     *
     * @param remoteClient 初始化时会默认传入
     * @see com.i360day.invoker.registry.AbstractRemoteScanAnnotationParser#registerInterfaceBean
     */
    public HttpInvokerClientFactoryBean(RemoteClientEntity remoteClient) {
        this.remoteClient = remoteClient;
    }


    /**
     * 初始化bean时检查属性
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        if (getServiceInterface() == null) {
            throw new IllegalArgumentException("Property 'serviceInterface' is required");
        }

        if (this.remoteClient == null) {
            throw new IllegalArgumentException(String.format("proxy %s, There is no remoteClient parameter in the constructor ", getServiceInterface()));
        }

        //代理对象
        this.targetProxy = new TargetProxy(
                getServiceInterface(),
                getServiceUrl(),
                getRemoteClient().getClientUser(),
                getRemoteClient().getClientPassword(),
                getBeanClassLoader()
        );
    }

    /**
     * 代理对象
     *
     * @return
     */
    @Override
    public Object getObject() {
        return getInvokerBuilder().build();
    }

    /**
     * 代理对象class
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return getServiceInterface();
    }

    /**
     * 是否单列模式
     *
     * @return
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * 使用目标代理类重写toString
     *
     * @return
     */
    @Override
    public String toString() {
        return getTargetProxy().toString();
    }

    /**
     * hashCode
     *
     * @return
     */
    @Override
    public int hashCode() {
        return getTargetProxy().hashCode();
    }

    /**
     * 获取代理详细对象
     *
     * @return
     */
    public TargetProxy getTargetProxy() {
        return targetProxy;
    }

    /**
     * 自定义上下文对象
     *
     * @return
     */
    protected InvokerContext getHttpInvokerContext() {
        if (this.invokerContext == null) {
            this.invokerContext = getApplicationContext().getBean(InvokerContext.class);
        }
        return invokerContext;
    }

    /**
     * 获取remoteClient详细对象
     *
     * @return
     */
    protected RemoteClientEntity getRemoteClient() {
        return remoteClient;
    }

    /**
     * get decoder
     *
     * @return
     */
    protected Decoder getDecoder() {
        //get Decoder
        Decoder decoder = getHttpInvokerContext().getBean(getDecoderClass(), true);
        if (decoder == null) {
            decoder = getHttpInvokerContext().createBean(getDecoderClass());
        }
        return decoder;
    }

    /**
     * 创建接口方法代理
     *
     * @return spring MethodInterceptor proxy
     */
    protected MethodInterceptor getMethodInterceptor() {

        //create Client Method Interceptor
        return new HttpInvokerClientMethodInterceptor(
                targetProxy,
                getHttpInvokerContext().getBean(HttpComponentsInvokerRequestExecutor.class),
                getHttpInvokerContext().getBean(RemoteInvocationFactory.class),
                getDecoder()
        );
    }

    /**
     * InvokerBuilder
     *
     * @return
     */
    protected InvokerBuilder getInvokerBuilder() {

        //create proxy
        return InvokerBuilder.create()
                .interfaceClass(getServiceInterface())
                .classLoader(getBeanClassLoader())
                .targetProxy(getTargetProxy())
                .fallback(getRemoteClient().getFallback())
                .httpInvokerContext(getHttpInvokerContext())
                .fallbackFactory(getRemoteClient().getFallbackFactory())
                .interceptor(getMethodInterceptor())
                .targeter(getHttpInvokerContext().getBean(Targeter.class))
                ;
    }
}
