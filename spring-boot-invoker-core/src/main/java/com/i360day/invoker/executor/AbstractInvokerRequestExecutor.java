/*
 * Copyright 2002-2018 the original author or authors.
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

package com.i360day.invoker.executor;

import com.i360day.invoker.BodyTemplate;
import com.i360day.invoker.RequestTemplate;
import com.i360day.invoker.http.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;


/**
 * Abstract base implementation of the HttpInvokerRequestExecutor interface.
 *
 * <p>Pre-implements serialization of RemoteInvocation objects and
 * deserialization of RemoteInvocationResults objects.
 *
 * @author Juergen Hoeller
 * @see #doExecuteRequest
 * @since 1.1
 */
abstract class AbstractInvokerRequestExecutor implements InvokerRequestExecutor, BeanClassLoaderAware {

    /**
     * Default content type: "application/x-java-serialized-object".
     */
    public static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

    protected static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    protected static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    protected static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

    protected static final String ENCODING_GZIP = "gzip";

    protected final Log logger = LogFactory.getLog(getClass());

    private String contentType = CONTENT_TYPE_SERIALIZED_OBJECT;

    private boolean acceptGzipEncoding = true;

    @Nullable
    private ClassLoader beanClassLoader;

    /**
     * Specify the content type to use for sending HTTP invoker requests.
     * <p>Default is "application/x-java-serialized-object".
     */
    public void setContentType(String contentType) {
        Assert.notNull(contentType, "'contentType' must not be null");
        this.contentType = contentType;
    }

    /**
     * Return the content type to use for sending HTTP invoker requests.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set whether to accept GZIP encoding, that is, whether to
     * send the HTTP "Accept-Encoding" header with "gzip" as value.
     * <p>Default is "true". Turn this flag off if you do not want
     * GZIP response compression even if enabled on the HTTP server.
     */
    public void setAcceptGzipEncoding(boolean acceptGzipEncoding) {
        this.acceptGzipEncoding = acceptGzipEncoding;
    }

    /**
     * Return whether to accept GZIP encoding, that is, whether to
     * send the HTTP "Accept-Encoding" header with "gzip" as value.
     */
    public boolean isAcceptGzipEncoding() {
        return this.acceptGzipEncoding;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    /**
     * Return the bean ClassLoader that this executor is supposed to use.
     */
    @Nullable
    protected ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }


    @Override
    public Response executeRequest(RequestTemplate requestTemplate) throws Exception {

        BodyTemplate bodyTemplate = requestTemplate.getBodyTemplate();
        if (logger.isDebugEnabled()) {
            logger.debug("Sending HTTP invoker request for service at [" + requestTemplate.getUri() + "], with size " + bodyTemplate.getSize());
        }
        return doExecuteRequest(requestTemplate);
    }

    /**
     * Execute a request to send the given serialized remote invocation.
     * <p>Implementations will usually call {@code readRemoteInvocationResult}
     * to deserialize a returned RemoteInvocationResult object.
     *
     * @return the RemoteInvocationResult object
     * @throws IOException            if thrown by I/O operations
     * @throws ClassNotFoundException if thrown during deserialization
     * @throws Exception              in case of general errors
     */
    protected abstract Response doExecuteRequest(RequestTemplate requestTemplate) throws Exception;

}
