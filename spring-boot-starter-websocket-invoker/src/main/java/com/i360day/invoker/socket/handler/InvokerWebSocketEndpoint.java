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
package com.i360day.invoker.socket.handler;

import com.i360day.invoker.WebSocketMessageListener;
import com.i360day.invoker.codes.WebSocketMessageSerialize;
import com.i360day.invoker.properties.InvokerWebSocketProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

/**
 * @author: liju.z
 * @date: 2024/4/1 13:04
 */
public class InvokerWebSocketEndpoint extends Endpoint {
    private final Logger logger = LoggerFactory.getLogger(InvokerWebSocketEndpoint.class);
    private InvokerWebSocketProperties invokerWebSocketProperties;
    private WebSocketMessageSerialize webSocketMessageSerialize;
    private final Map<String, WebSocketMessageListener> serviceExporterMap;

    /**
     * @see com.i360day.invoker.socket.WebSocketHttpRequestHandler#handleRequest
     * @param invokerWebSocketProperties
     * @param serviceExporterMap
     */
    public InvokerWebSocketEndpoint(WebSocketMessageSerialize webSocketMessageSerialize, InvokerWebSocketProperties invokerWebSocketProperties, Map<String, WebSocketMessageListener> serviceExporterMap) {
        this.webSocketMessageSerialize = webSocketMessageSerialize;
        this.serviceExporterMap = serviceExporterMap;
        this.invokerWebSocketProperties = invokerWebSocketProperties;

    }


    /**
     * 握手成功
     * @param session the session that has just been activated.
     * @param config  the configuration used to configure this endpoint.
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        //设置最大空闲超时
        session.setMaxIdleTimeout(this.invokerWebSocketProperties.getMaxIdleTimeout());
        //设置最大二进制消息缓冲区大小
        session.setMaxBinaryMessageBufferSize(this.invokerWebSocketProperties.getMaxBinaryMessageBufferSize());
        //设置最大文本消息缓冲区大小
        session.setMaxTextMessageBufferSize(this.invokerWebSocketProperties.getMaxTextMessageBufferSize());
        //监听事件
        session.addMessageHandler(new InvokerMessageByteBufferHandler(this, session, webSocketMessageSerialize, this.serviceExporterMap));
        //ping
        session.addMessageHandler(new InvokerMessagePingHandler(session));

        if(logger.isInfoEnabled()){
            logger.info("webSocket id => {} client => {} to server => {} rpc open",
                    session.getPathParameters().get("Client-Id"),
                    session.getPathParameters().get("clientIp"),
                    session.getRequestURI()
            );
        }
    }

    /**
     * 关闭监听
     * @param session     the session about to be closed.
     * @param closeReason the reason the session was closed.
     */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        try {
            if(logger.isWarnEnabled()){
                logger.warn("webSocket id => {} client => {} to server => {} rpc close",
                        session.getPathParameters().get("Client-Id"),
                        session.getPathParameters().get("clientIp"),
                        session.getRequestURI()
                );
            }

            if (session.isOpen()) {
                session.close(closeReason);
            }
        } catch (IOException ex) {
            if(logger.isErrorEnabled()){
                logger.error("webSocket id => {} client => {} to server => {} rpc close error {}",
                        session.getPathParameters().get("Client-Id"),
                        session.getPathParameters().get("clientIp"),
                        session.getRequestURI(),
                        ex
                );
            }
        }
    }

    /**
     * 错误监听
     * @param session the session in use when the error occurs.
     * @param thr     the throwable representing the problem.
     */
    @Override
    public void onError(Session session, Throwable thr) {
        if(logger.isErrorEnabled()){
            logger.error("webSocket id => {} client => {} to server => {} rpc error",
                    session.getPathParameters().get("Client-Id"),
                    session.getPathParameters().get("clientIp"),
                    session.getRequestURI()
            );
        }
    }
}
