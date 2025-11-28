/*
 * Copyright (c) 1994, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.i360day.invoker.configuration;

import com.i360day.invoker.WebSocketInvokerRequest;
import com.i360day.invoker.WebSocketInvokerServiceExporter;
import com.i360day.invoker.WebSocketMessageListener;
import com.i360day.invoker.codes.WebSocketMessageSerialize;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.executor.WebSocketInvokerRequestExecutor;
import com.i360day.invoker.interceptor.InvokerRequestInterceptor;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.properties.InvokerWebSocketProperties;
import com.i360day.invoker.socket.WebSocketHttpRequestHandler;
import com.i360day.invoker.socket.handler.InvokerWebSocketEndpoint;
import com.i360day.invoker.socket.handler.InvokerWebSocketHandlerMapping;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * implements WebSocketConfigurer
 *
 * @author: liju.z
 * @date: 2023/12/4 14:07
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(InvokerAutoConfiguration.class)
@EnableConfigurationProperties(InvokerWebSocketProperties.class)
public class InvokerWebSocketConfiguration {

    /**
     * 注册socket to mapping
     *
     * @param webSocketHttpRequestHandler
     * @param invokerWebSocketProperties
     * @return
     */
    @Bean
    public HandlerMapping httpInvokerWebSocketHandlerMapping(WebSocketHttpRequestHandler webSocketHttpRequestHandler, InvokerWebSocketProperties invokerWebSocketProperties) {
        Map<String, WebSocketHttpRequestHandler> urlParams = Collections.singletonMap(invokerWebSocketProperties.getEndpoint() + "/**", webSocketHttpRequestHandler);
        return new InvokerWebSocketHandlerMapping(urlParams, 1);
    }

    /**
     * upgrade Http To WebSocket handler
     *
     * @param applicationContext
     * @param invokerWebSocketProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public WebSocketHttpRequestHandler webSocketHttpRequestHandler(ApplicationContext applicationContext,
                                                                   WebSocketMessageSerialize webSocketMessageSerialize,
                                                                   InvokerWebSocketProperties invokerWebSocketProperties) {
        //get service export
        Map<String, WebSocketInvokerServiceExporter> serviceExporterMap = applicationContext.getBeansOfType(WebSocketInvokerServiceExporter.class);
        Map<String, WebSocketMessageListener> webSocketMessageListenerMap = serviceExporterMap.keySet().stream().collect(Collectors.toMap(k -> k, v -> serviceExporterMap.get(v)));
        InvokerWebSocketEndpoint invokerWebSocketEndpoint = new InvokerWebSocketEndpoint(webSocketMessageSerialize, invokerWebSocketProperties, webSocketMessageListenerMap);

        //upgrade Http To WebSocket
        return new WebSocketHttpRequestHandler(invokerWebSocketProperties, invokerWebSocketEndpoint);
    }

    /**
     * create WebSocket Request
     *
     * @param invokerProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public WebSocketInvokerRequest webSocketHttpInvokerRequest(WebSocketMessageSerialize webSocketMessageSerialize,
                                                               InvokerProperties invokerProperties,
                                                               InvokerWebSocketProperties invokerWebSocketProperties) {
        return new WebSocketInvokerRequest(webSocketMessageSerialize, invokerProperties, invokerWebSocketProperties);
    }

    /**
     * create webSocket request executor
     *
     * @param invokerRequest
     * @param invokerContext
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public WebSocketInvokerRequestExecutor webSocketHttpInvokerRequestExecutor(WebSocketInvokerRequest invokerRequest, InvokerContext invokerContext) {
        return new WebSocketInvokerRequestExecutor(invokerRequest, invokerContext.getBeanOfMap(InvokerRequestInterceptor.class).values());
    }

    /**
     * webSocket消息序列化
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public WebSocketMessageSerialize webSocketMessageSerialize() {
        return new WebSocketMessageSerialize();
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 创建默认请求拦截器 <p>
     *
     * <p> @Date  21:03 <p>
     *
     * <p> @return HttpInvokerRequestInterceptor <p>
     **/
    @Bean
    @ConditionalOnMissingBean
    public InvokerRequestInterceptor simpleRequestInterceptor() {
        return requestTemplate -> {

        };
    }
}
