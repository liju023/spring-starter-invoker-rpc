package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteClientEntity;
import com.i360day.invoker.executor.WebSocketInvokerRequestExecutor;
import com.i360day.invoker.support.RemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * redis proxy interface bean
 */
public class WebSocketInvokerClientFactoryBean extends HttpInvokerClientFactoryBean {

    /**
     * create redis proxy client
     *
     * @param remoteClient
     * @see com.i360day.invoker.annotation.RemoteWebSocketClient#clientProxyClass
     */
    public WebSocketInvokerClientFactoryBean(RemoteClientEntity remoteClient) {
        super(remoteClient);
    }

    /**
     * 创建webSocket代理
     *
     * @return
     */
    @Override
    protected MethodInterceptor getMethodInterceptor() {

        //create Client Method Interceptor
        return new HttpInvokerClientMethodInterceptor(
                getTargetProxy(),
                getHttpInvokerContext().getBean(WebSocketInvokerRequestExecutor.class),
                getHttpInvokerContext().getBean(RemoteInvocationFactory.class),
                getDecoder()
        );
    }


    @Override
    public String toString() {
        return getTargetProxy().toString();
    }
}
