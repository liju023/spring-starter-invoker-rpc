package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteClientEntity;
import com.i360day.invoker.executor.CloudInvokerRequestExecutor;
import com.i360day.invoker.support.RemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * cloud client proxy interface bean
 */
public class CloudInvokerClientFactoryBean extends HttpInvokerClientFactoryBean {

    /**
     * create cloud proxy client
     *
     * @param remoteClient
     */
    public CloudInvokerClientFactoryBean(RemoteClientEntity remoteClient) {
        super(remoteClient);
    }

    /**
     * cloud 接口代理
     *
     * @return
     */
    @Override
    protected MethodInterceptor getMethodInterceptor() {

        //create Client Method Interceptor
        return new HttpInvokerClientMethodInterceptor(
                getTargetProxy(),
                getHttpInvokerContext().getBean(CloudInvokerRequestExecutor.class),
                getHttpInvokerContext().getBean(RemoteInvocationFactory.class),
                getDecoder()
        );
    }

    @Override
    public String toString() {
        return getTargetProxy().toString();
    }
}
