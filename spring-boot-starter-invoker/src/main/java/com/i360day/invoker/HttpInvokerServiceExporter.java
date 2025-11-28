/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i360day.invoker;

import com.i360day.invoker.codes.encoder.Encoder;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.exception.InvalidContentTypeException;
import com.i360day.invoker.exception.RemoteClientUserException;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.support.RemoteInvocation;
import com.i360day.invoker.support.RemoteInvocationFactory;
import com.i360day.invoker.support.RemoteInvocationResult;
import com.i360day.invoker.support.RemoteInvocationSerializingExporter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author liju.z
 * @see HttpInvokerClientFactoryBean
 * @since 1.1
 */
public class HttpInvokerServiceExporter extends RemoteInvocationSerializingExporter implements HttpRequestHandler, InitializingBean {
    /**
     * 指定响应content-type
     */
    private final MediaType contentType = MediaType.parseMediaType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);

    /**
     * 编码器
     */
    private Encoder encoder;

    /**
     * global properties
     */
    private InvokerProperties invokerProperties;

    /**
     * constructor
     *
     * @param remoteInvocationFactory
     */
    public HttpInvokerServiceExporter(RemoteInvocationFactory remoteInvocationFactory) {
        super(remoteInvocationFactory);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        //获取 Encoder
        InvokerContext invokerContext = getApplicationContext().getBean(InvokerContext.class);
        Encoder encoder = invokerContext.getBean(getEncoderClass(), true);
        if (encoder == null) {
            encoder = invokerContext.createBean(getEncoderClass());
        }
        setEncoder(encoder);

        // HttpInvokerProperties
        this.invokerProperties = invokerContext.getBean(InvokerProperties.class);
    }

    /**
     * 编码器
     *
     * @return
     */
    protected Encoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    public InvokerProperties getHttpInvokerProperties() {
        return invokerProperties;
    }

    /**
     * 验证客户端用户
     *
     * @param clientAuthorization
     */
    protected void verifyUser(String clientAuthorization) {
        //Verify account password
        InvokerProperties.HttpInvokerServerProperties globalServer = getHttpInvokerProperties().getGlobalServer();
        if (globalServer != null && ObjectUtils.isNotEmpty(globalServer.getServerUser()) && ObjectUtils.isNotEmpty(globalServer.getServerPassword())) {
            String serverAuthorization = String.format("%s:%s", globalServer.getServerUser(), globalServer.getServerPassword());
            if (!ObjectUtils.isEquals(serverAuthorization, clientAuthorization)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("client auth error， Account or password mismatch client {}, server {}", clientAuthorization, serverAuthorization);
                }
                throw new RemoteClientUserException("client auth error， Account or password mismatch");
            }
        }
    }

    /**
     * Reads a remote invocation from the request, executes it,
     * and writes the remote invocation result to the response.
     *
     * @see #readRemoteInvocation(HttpServletRequest)
     * @see #invokeAndCreateResult(RemoteInvocation, Object)
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //default
        RemoteInvocationResult result;
        Type returnType = null;

        try {
            //verify user
            verifyUser(request.getHeader(InvokerConstant.AUTHORIZATION));

            //read
            RemoteInvocation invocation = readRemoteInvocation(request);
            returnType = invocation.getReturnType(getService());

            //invoke
            result = invokeAndCreateResult(invocation, getProxy());

        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("http invoker service handler error {}", ex);
            }

            // 403
            if (ex instanceof InvalidContentTypeException || ex instanceof RemoteClientUserException) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            }

            result = new RemoteInvocationResult(ex);
        }

        //write
        encoder(response, Optional.ofNullable(returnType).orElse(Object.class), result);
    }

    /**
     * Write the given RemoteInvocationResult to the given HTTP response.
     *
     * @param response     current HTTP response
     * @param responseType current HTTP responseType
     * @param result       the RemoteInvocationResult object
     */
    protected void encoder(HttpServletResponse response, Type responseType, RemoteInvocationResult result) {
        try {
            response.setContentType(contentType.toString());

            if (result.getException() != null || responseType == null) {
                int httpStatusCode = response.getStatus() == HttpStatus.OK.value() ? HttpStatus.INTERNAL_SERVER_ERROR.value() : response.getStatus();
                writeMessage(response, httpStatusCode, result.getStackTrace());
            } else {
                getEncoder().encoder(response.getOutputStream(), responseType, result);
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("write response error ", e);
            }
            writeMessage(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    /**
     * response out message
     *
     * @param response
     * @param code
     * @param message
     */
    private void writeMessage(HttpServletResponse response, int code, String message) {
        try {
            response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));

            if (response.getStatus() != 200) {
                code = response.getStatus();
                response.sendError(code, message);
            }
            response.setStatus(code);

        } catch (IOException ex) {
            logger.error("http invoker server out message error...{}", ex);
        }
    }

    /**
     * Apply the given remote invocation to the given target object, wrapping
     * the invocation result in a serializable RemoteInvocationResult object.
     * The default implementation creates a plain RemoteInvocationResult.
     * <p>Can be overridden in subclasses for custom invocation behavior,
     * for example to return additional context information. Note that this
     * is not covered by the RemoteInvocationExecutor strategy!
     *
     * @param invocation   the remote invocation
     * @param targetObject the target object to apply the invocation to
     * @return the invocation result
     * @see #invoke
     */
    protected RemoteInvocationResult invokeAndCreateResult(RemoteInvocation invocation, Object targetObject) {
        try {
            Object value = invoke(invocation, targetObject);
            return new RemoteInvocationResult(value);
        } catch (Throwable ex) {
            return new RemoteInvocationResult(ex);
        }
    }

    /**
     * Read a RemoteInvocation from the given HTTP request.
     * the {@link HttpServletRequest#getInputStream() servlet request's input stream}.
     *
     * @param request current HTTP request
     * @return the RemoteInvocation object
     * @throws IOException            in case of I/O failure
     * @throws ClassNotFoundException if thrown by deserialization
     */
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request) throws IOException, ClassNotFoundException {
        if (!getContentType().equals(request.getContentType())) {
            throw new InvalidContentTypeException(String.format("request header contentType [%s] mismatched, server only Accept %s", request.getContentType(), getContentType()));
        }
        return doReadRemoteInvocation(request.getInputStream());
    }

}
