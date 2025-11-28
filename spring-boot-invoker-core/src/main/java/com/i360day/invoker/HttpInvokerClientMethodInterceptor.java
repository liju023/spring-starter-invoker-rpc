package com.i360day.invoker;

import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.exception.InvokerException;
import com.i360day.invoker.exception.RemoteAccessException;
import com.i360day.invoker.exception.RemoteConnectFailureException;
import com.i360day.invoker.exception.RemoteInvocationFailureException;
import com.i360day.invoker.executor.InvokerRequestExecutor;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.proxy.TargetProxy;
import com.i360day.invoker.support.RemoteInvocation;
import com.i360day.invoker.support.RemoteInvocationFactory;
import com.i360day.invoker.support.RemoteInvocationResult;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InvalidClassException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Optional;

/**
 * <p> @description: spring 方法代理，支持spring AOP逻辑    <p>
 * <p>
 * <p> @author: 胡.青牛   <p>
 * <p>
 * <p> @date: 2019/5/10 0010  15:42
 **/
public final class HttpInvokerClientMethodInterceptor implements MethodInterceptor {

    private Decoder decoder;
    private TargetProxy targetProxy;

    private InvokerRequestExecutor invokerRequestExecutor;

    private RemoteInvocationFactory remoteInvocationFactory;

    /**
     * 方法代理
     *
     * @param targetProxy
     * @param invokerRequestExecutor
     * @param remoteInvocationFactory
     */
    public HttpInvokerClientMethodInterceptor(TargetProxy targetProxy,
                                              InvokerRequestExecutor invokerRequestExecutor,
                                              RemoteInvocationFactory remoteInvocationFactory,
                                              Decoder decoder) {
        this.targetProxy = targetProxy;
        this.remoteInvocationFactory = remoteInvocationFactory;
        this.invokerRequestExecutor = invokerRequestExecutor;
        this.decoder = decoder;
    }

    /**
     * <p> @Description:  <p>
     *
     * <p> @author: 青牛.胡 <p>
     *
     * <p> @Date:   2019/6/24 0024 10:32 <p>
     *
     * <p> @param ex      具体错误 </p>
     * <p> @param config  config </p>
     *
     * <p> @return:  错误  <p>
     **/
    protected RemoteAccessException convertHttpInvokerAccessException(Throwable ex) {
        if (ex instanceof ConnectException) {
            return new RemoteConnectFailureException(
                    "Could not connect to HTTP invoker remote service at [" + Arrays.asList(targetProxy.getUrl()) + "]", ex);
        }

        if (ex instanceof ClassNotFoundException || ex instanceof NoClassDefFoundError ||
                ex instanceof InvalidClassException) {
            return new RemoteAccessException(
                    "Could not deserialize result from HTTP invoker remote service [" + Arrays.asList(targetProxy.getUrl()) + "]", ex);
        }

        if (ex instanceof IOException) {
            return new RemoteAccessException(
                    ex.getMessage() + "，Could not access HTTP invoker remote service at [" + Arrays.asList(targetProxy.getUrl()) + "]", ex);
        }

        if (ex instanceof Exception) {
            return new RemoteAccessException(
                    "Could not access HTTP invoker remote service at [" + Arrays.asList(targetProxy.getUrl()) + "]", ex);
        }
        // For any other Throwable, e.g. OutOfMemoryError: let it get propagated as-is.
        return null;
    }

    protected RemoteInvocationResult executeRequest(RequestTemplate requestTemplate) throws Exception {
        Response response = invokerRequestExecutor.executeRequest(requestTemplate);
        if(response.getStatus() != 200){
            throw new IllegalStateException(
                    String.format("network status [%s] error, request uri [%s], result [%s]",
                            response.getStatus(),
                            requestTemplate.getUri(),
                            IOUtils.toString(response.getBody().asInputStream()))
            );
        }
        //decode
        return decoder.decode(response, requestTemplate.getMethodMetadata().getReturnType());
    }

    /**
     * <p> @Description: 配置相关参数，发起预请求   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/11 0011 09:55   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    protected Object execute(RequestTemplate requestTemplate) throws Throwable {
        RemoteInvocationResult result;
        //网络异常
        try {
            result = executeRequest(requestTemplate);
        } catch (Throwable ex) {
            RemoteAccessException rae = convertHttpInvokerAccessException(ex);
            throw (rae != null ? rae : ex);
        }

        //数据异常
        try {
            return result.recreate();
        } catch (Throwable ex) {
            if (result.hasInvocationTargetException()) {
                throw ex;
            } else {
                throw new RemoteInvocationFailureException("Invocation of method [" + requestTemplate.getMethodMetadata().getMethod().toString() + "] failed in HTTP invoker remote service at [" + requestTemplate.getUri() + "]", ex);
            }
        }
    }

    /**
     * 执行代理接口方法
     *
     * @param invocation the method invocation joinpoint
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return Optional.ofNullable(ObjectUtils.isToString(this, invocation)).orElseGet(() -> {
            try {
                RemoteInvocation remoteInvocation = remoteInvocationFactory.createRemoteInvocation(invocation);
                MethodMetadata methodMetadata = MethodMetadata.of(invocation.getMethod(), invocation.getArguments());

                //构建templete
                RequestTemplate requestTemplate = RequestTemplate.of(targetProxy, methodMetadata, remoteInvocation);
                return execute(requestTemplate);
            } catch (Throwable throwable) {
                throw new InvokerException(throwable);
            }
        });
    }


    @Override
    public String toString() {
        return targetProxy.toString();
    }
}
