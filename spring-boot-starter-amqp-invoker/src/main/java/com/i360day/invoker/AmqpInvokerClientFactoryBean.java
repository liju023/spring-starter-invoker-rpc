package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteClientEntity;
import com.i360day.invoker.executor.AmqpInvokerRequestExecutor;
import com.i360day.invoker.support.RemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * redis proxy interface bean
 */
public class AmqpInvokerClientFactoryBean extends HttpInvokerClientFactoryBean {

    /**
     * create redis proxy client
     *
     * @param remoteClient
     */
    public AmqpInvokerClientFactoryBean(RemoteClientEntity remoteClient) {
        super(remoteClient);

    }

    /**
     * 创建 amqp 代理
     *
     * @return
     */
    @Override
    protected MethodInterceptor getMethodInterceptor() {

        //create Client Method Interceptor
        return new HttpInvokerClientMethodInterceptor(
                getTargetProxy(),
                getHttpInvokerContext().getBean(AmqpInvokerRequestExecutor.class),
                getHttpInvokerContext().getBean(RemoteInvocationFactory.class),
                getDecoder()
        );
    }


    @Override
    public String toString() {
        return getTargetProxy().toString();
    }
}
