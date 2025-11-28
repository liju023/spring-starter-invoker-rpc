package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteClientEntity;
import com.i360day.invoker.executor.RedisInvokerRequestExecutor;
import com.i360day.invoker.support.RemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * redis proxy interface bean
 */
public class RedisInvokerClientFactoryBean extends HttpInvokerClientFactoryBean {

    /**
     * create redis proxy client
     *
     * @param remoteClient
     */
    public RedisInvokerClientFactoryBean(RemoteClientEntity remoteClient) {
        super(remoteClient);
    }

    /**
     * 创建redis代理
     *
     * @return
     */
    @Override
    protected MethodInterceptor getMethodInterceptor() {

        //create Client Method Interceptor
        return new HttpInvokerClientMethodInterceptor(
                getTargetProxy(),
                getHttpInvokerContext().getBean(RedisInvokerRequestExecutor.class),
                getHttpInvokerContext().getBean(RemoteInvocationFactory.class),
                getDecoder()
        );
    }

    @Override
    public String toString() {
        return getTargetProxy().toString();
    }
}
