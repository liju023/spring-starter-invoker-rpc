/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.i360day.invoker.socket;

import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.configuration.InvokerWebSocketConfiguration;
import com.i360day.invoker.properties.InvokerWebSocketProperties;
import com.i360day.invoker.socket.handler.InvokerWebSocketEndpoint;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;

import io.undertow.websockets.jsr.ServerWebSocketContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author liju.z
 * @date 2024/1/30 20:56
 */
public class WebSocketHttpRequestHandler implements HttpRequestHandler {
    private final Logger logger = LoggerFactory.getLogger(WebSocketHttpRequestHandler.class);
    private final InvokerWebSocketEndpoint invokerWebSocketEndpoint;
    private InvokerWebSocketProperties invokerWebSocketProperties;

    /**
     * upgrade Http To WebSocket
     *
     * @param invokerWebSocketEndpoint
     * @see InvokerWebSocketConfiguration#webSocketHttpRequestHandler
     */
    public WebSocketHttpRequestHandler(InvokerWebSocketProperties invokerWebSocketProperties, InvokerWebSocketEndpoint invokerWebSocketEndpoint) {
        this.invokerWebSocketEndpoint = invokerWebSocketEndpoint;
        this.invokerWebSocketProperties = invokerWebSocketProperties;
    }

    /**
     * 处理http 提升为 socket协议
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if(ObjectUtils.isNotEmpty(invokerWebSocketProperties.getServerUserName()) && ObjectUtils.isNotEmpty(invokerWebSocketProperties.getServerPassword())){

                if (!ObjectUtils.isEquals(invokerWebSocketProperties.getServerUserName(), request.getHeader("username"))
                        && !ObjectUtils.isEquals(invokerWebSocketProperties.getServerPassword(), request.getHeader("password"))) {
                    logger.warn("server -> username={}, password={} | client -> username={}, password={} 账号密码错误",
                            invokerWebSocketProperties.getServerUserName(),
                            invokerWebSocketProperties.getServerPassword(),
                            request.getHeader("username"),
                            request.getHeader("password")
                    );
                    return;
                }

            }

            //create
            ServerWebSocketContainer container = (ServerWebSocketContainer) getContainer(request);
            ServerEndpointRegistration serverEndpointRegistration = new ServerEndpointRegistration(request.getRequestURI(), invokerWebSocketEndpoint);

            //request params
            Map<String, String> params = new LinkedHashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String key = parameterNames.nextElement();
                params.put(key, request.getParameter(key));
            }
            params.put("Client-Id", request.getHeader("Client-Id"));
            params.put("username", request.getHeader("username"));
            params.put("password", request.getHeader("password"));
            params.put("clientIp", String.format("%s:%s", request.getRemoteAddr(), request.getRemotePort()));

            container.doUpgrade(request, response, serverEndpointRegistration, params);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取server容器
     *
     * @param request
     * @return
     */
    protected ServerContainer getContainer(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        String attrName = "javax.websocket.server.ServerContainer";
        ServerContainer container = (ServerContainer) servletContext.getAttribute(attrName);
        Assert.notNull(container, "No 'javax.websocket.server.ServerContainer' ServletContext attribute. Are you running in a Servlet container that supports JSR-356?");
        return container;
    }
}
