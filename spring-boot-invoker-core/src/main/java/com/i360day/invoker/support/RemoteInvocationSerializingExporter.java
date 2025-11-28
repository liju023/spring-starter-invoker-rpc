/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i360day.invoker.support;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.*;

/**
 * Abstract base class for remote service exporters that explicitly deserialize
 * for example Spring's HTTP invoker.
 *
 * <p>Provides template methods for {@code ObjectInputStream} and
 * {@code ObjectOutputStream} handling.
 *
 * @author Juergen Hoeller
 * @see java.io.ObjectInputStream
 * @see java.io.ObjectOutputStream
 * @since 2.5.1
 */
public abstract class RemoteInvocationSerializingExporter extends RemoteInvocationBasedExporter implements InitializingBean, ApplicationContextAware {

    /**
     * Default content type: "application/x-java-serialized-object".
     */
    public static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";
    /**
     * 数据包转换 RemoteInvocation
     */
    private final RemoteInvocationFactory remoteInvocationFactory;
    /**
     * 默认协议头
     */
    private String contentType = CONTENT_TYPE_SERIALIZED_OBJECT;
    /**
     * spring 上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 是否开启代理
     */
    private boolean acceptProxyClasses = true;
    /**
     * 代理对象
     */
    private Object proxy;

    /**
     * @param remoteInvocationFactory
     */
    protected RemoteInvocationSerializingExporter(RemoteInvocationFactory remoteInvocationFactory) {
        this.remoteInvocationFactory = remoteInvocationFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Specify the content type to use for sending remote invocation responses.
     * <p>Default is "application/x-java-serialized-object".
     */
    public void setContentType(String contentType) {
        Assert.notNull(contentType, "'contentType' must not be null");
        this.contentType = contentType;
    }

    /**
     * Return the content type to use for sending remote invocation responses.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set whether to accept deserialization of proxy classes.
     * <p>Default is "true". May be deactivated as a security measure.
     */
    public void setAcceptProxyClasses(boolean acceptProxyClasses) {
        this.acceptProxyClasses = acceptProxyClasses;
    }

    /**
     * Return whether to accept deserialization of proxy classes.
     */
    public boolean isAcceptProxyClasses() {
        return this.acceptProxyClasses;
    }


    @Override
    public void afterPropertiesSet() {
        //如果service为空，使用代理接口获取service实列
        if (getService() == null) {
            Object service = getApplicationContext().getBean(getServiceInterface());
            setService(service);
        }

        //Initialize this service exporter.
        prepare();
    }

    /**
     * Initialize this service exporter.
     */
    public void prepare() {
        this.proxy = getProxyForService();
    }

    /**
     * 获取service的代理
     * @return
     */
    protected final Object getProxy() {
        if (this.proxy == null) {
            throw new IllegalStateException(ClassUtils.getShortName(getClass()) + " has not been initialized");
        }
        return this.proxy;
    }


    /**
     * Create an ObjectInputStream for the given InputStream.
     * <p>The default implementation creates a Spring {@link CodebaseAwareObjectInputStream}.
     *
     * @param is the InputStream to read from
     * @return the new ObjectInputStream instance to use
     * @throws java.io.IOException if creation of the ObjectInputStream failed
     */
    protected ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
        return new CodebaseAwareObjectInputStream(is, getBeanClassLoader(), isAcceptProxyClasses());
    }

    /**
     * Perform the actual reading of an invocation result object from the
     * given ObjectInputStream.
     * <p>The default implementation simply calls
     * {@link java.io.ObjectInputStream#readObject()}.
     * Can be overridden for deserialization of a custom wrapper object rather
     * than the plain invocation, for example an encryption-aware holder.
     *
     * @param ois the ObjectInputStream to read from
     * @return the RemoteInvocationResult object
     * @throws java.io.IOException    in case of I/O failure
     * @throws ClassNotFoundException if case of a transferred class not
     *                                being found in the local ClassLoader
     */
    protected RemoteInvocation doReadRemoteInvocation(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = createObjectInputStream(is);
        try {
            return doReadRemoteInvocation(inputStream);
        } finally {
            inputStream.close();
        }
    }

    /**
     * Perform the actual reading of an invocation result object from the
     * given ObjectInputStream.
     * <p>The default implementation simply calls
     * {@link java.io.ObjectInputStream#readObject()}.
     * Can be overridden for deserialization of a custom wrapper object rather
     * than the plain invocation, for example an encryption-aware holder.
     *
     * @param ois the ObjectInputStream to read from
     * @return the RemoteInvocationResult object
     * @throws java.io.IOException    in case of I/O failure
     * @throws ClassNotFoundException if case of a transferred class not
     *                                being found in the local ClassLoader
     */
    protected RemoteInvocation doReadRemoteInvocation(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        return this.remoteInvocationFactory.doReadRemoteInvocation(ois);
    }


    public void write(OutputStream outputStream, RemoteInvocationResult result) throws IOException {
        FlushGuardedOutputStream flushGuardedOutputStream = new FlushGuardedOutputStream(outputStream);
        ObjectOutputStream oos = new ObjectOutputStream(flushGuardedOutputStream);
        try {
            oos.writeObject(result);
        } finally {
            oos.close();
        }
    }

    public void write(HttpServletResponse response, RemoteInvocationResult result) throws IOException {
        write(response.getOutputStream(), result);
    }

    /**
     * Decorate an {@code OutputStream} to guard against {@code flush()} calls,
     * which are turned into no-ops.
     * <p>Because {@link ObjectOutputStream#close()} will in fact flush/drain
     * the underlying stream twice, this {@link FilterOutputStream} will
     * guard against individual flush calls. Multiple flush calls can lead
     * to performance issues, since writes aren't gathered as they should be.
     *
     * @see <a href="https://jira.spring.io/browse/SPR-14040">SPR-14040</a>
     */
    private static class FlushGuardedOutputStream extends FilterOutputStream {

        public FlushGuardedOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void flush() throws IOException {
            // Do nothing on flush
        }
    }
}
