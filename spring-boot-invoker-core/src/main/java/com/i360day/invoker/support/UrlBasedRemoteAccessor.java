/*
 * Copyright 2002-2012 the original author or authors.
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

import com.i360day.invoker.codes.decoder.Decoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URI;

/**
 * Abstract base class for classes that access remote services via URLs.
 * Provides a "serviceUrl" bean property, which is considered as required.
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 */
public abstract class UrlBasedRemoteAccessor extends RemoteAccessor implements InitializingBean, ApplicationContextAware {
    /**
     * context
     */
    private ApplicationContext applicationContext;
    /**
     * remote service url
     */
    private URI[] serviceUrl;
    /**
     * 解码器
     */
    private Class<Decoder> decoderClass;

    /**
     * set
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * get application context
     *
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Set the URL of this remote accessor's target service.
     * The URL must be compatible with the rules of the particular remoting provider.
     */
    public void setServiceUrl(URI[] serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Return the URL of this remote accessor's target service.
     */
    public URI[] getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * get 实现Decoder类的class
     * @return
     */
    public Class<Decoder> getDecoderClass() {
        return decoderClass;
    }

    /**
     * set 实现Decoder类的class
     * @param decoderClass
     */
    public void setDecoderClass(Class<Decoder> decoderClass) {
        this.decoderClass = decoderClass;
    }

    @Override
    public void afterPropertiesSet() {
        if (getServiceUrl() == null) {
            throw new IllegalArgumentException("Property 'serviceUrl' is required");
        }

        if (getDecoderClass() == null) {
            throw new IllegalArgumentException("Property 'decoderClass' is required");
        }
    }

}
