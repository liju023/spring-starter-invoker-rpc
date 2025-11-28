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
package com.i360day.invoker.socket.connection;

import com.i360day.invoker.WebSocketInvokerRequest;
import com.i360day.invoker.exception.WebSocketConnectionErrorException;
import com.i360day.invoker.properties.InvokerWebSocketProperties;
import com.i360day.invoker.support.AbstractMessageContainerSmartLifecycle;
import javax.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * webSocket connection factory
 *
 * @author liju.z
 * @date 2025/3/30 8:20
 * @see WebSocketInvokerRequest#getWebsocketConnectionFactory
 */
public class SimpleWebSocketConnectionFactory extends AbstractMessageContainerSmartLifecycle implements InitializingBean, Runnable, MessageHandler.Whole<ByteBuffer>, WebSocketConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(SimpleWebSocketConnectionFactory.class);
    private InvokerWebSocketProperties invokerWebSocketProperties;
    /**
     * client config
     */
    private ClientEndpointConfig clientEndpointConfig;
    /**
     * webSocket address
     */
    private URI address;
    /**
     * session
     */
    private Session session;
    /**
     * locak
     */
    private final Lock lifecycleLock = new ReentrantLock();
    /**
     * 消息处理
     */
    private List<MessageHandler> messageHandlerList = new LinkedList<>();
    /**
     * 监听器
     */
    private List<WebSocketConnectionMessageListener> connectionMessageListenerList = new LinkedList<>();

    /**
     * 创建socket连接工厂
     *
     * @param invokerWebSocketProperties
     * @param address
     */
    public SimpleWebSocketConnectionFactory(InvokerWebSocketProperties invokerWebSocketProperties, URI address) {
        this.address = address;
        this.invokerWebSocketProperties = invokerWebSocketProperties;
    }

    public void setClientEndpointConfig(ClientEndpointConfig clientEndpointConfig) {
        this.clientEndpointConfig = clientEndpointConfig;
    }

    @Override
    public Session getSession() {
        return this.session;
    }

    /**
     * 添加session MessageHandler
     *
     * @param messageHandler
     */
    public void addConnectionMessageHandler(MessageHandler messageHandler) {
        this.messageHandlerList.add(messageHandler);
    }

    /**
     * 添加连接消息监听
     *
     * @param connectionMessageListener
     */
    public void addConnectionMessageListener(WebSocketConnectionMessageListener connectionMessageListener) {
        this.connectionMessageListenerList.add(connectionMessageListener);
    }

    /**
     * 判断
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.address, "websocket address is required");
    }

    /**
     * 启动
     */
    @Override
    protected void doStart() {

        try {
            this.session = createWebsocketConnection();
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("initialization socket address {} error {}", address, ex);
            }
            throw new WebSocketConnectionErrorException(ex);
        }

        getTaskExecutor().execute(this);
    }

    /**
     * stop
     */
    @Override
    protected void doStop() {
        super.doStop();

        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException ex) {
                if (logger.isErrorEnabled()) {
                    logger.error("session close error", ex);
                }
            }
        }
    }

    /**
     * 消息处理
     *
     * @param message the message data.
     */
    @Override
    public void onMessage(ByteBuffer message) {
        try {
            connectionMessageListenerList.forEach(messageListener -> messageListener.onMessage(message));
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("websocket handler message ", ex);
            }
        }
    }

    /**
     * 创建socket连接
     *
     * @return
     */
    private Session createWebsocketConnection() {
        try {
            lifecycleLock.lock();
            WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
            //configure
            ClientEndpointConfig endpointConfig = Optional.ofNullable(clientEndpointConfig).orElse(
                    ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {
                            headers.put("Client-Id", Arrays.asList(UUID.randomUUID().toString()));
                        }
                    }).build()
            );

            //create Connection
            Session session = webSocketContainer.connectToServer(new InternalEndpoint(), endpointConfig, address);
            session.addMessageHandler(this);
            return session;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            lifecycleLock.unlock();
        }
    }

    /**
     * 重置socket连接
     */
    private void restart() {
        try {
            //verify session
            if (session != null && session.isOpen()) {
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "链接重置"));
                } catch (IOException ex) {
                    if (logger.isDebugEnabled()) {
                        logger.error("webSocket {} session close error {}", address, ex);
                    }
                }
            }

            //create webSocket
            this.session = createWebsocketConnection();

            if (logger.isInfoEnabled()) {
                logger.info("webSocket {} restart success", address);
            }

        } catch (Exception ex) {
            //ignore ex
            if (logger.isWarnEnabled()) {
                logger.warn("webSocket {} restart error ", address);
            }
        }
    }

    /**
     * 执行
     */
    @Override
    public void run() {

        while (isRunning()) {
            try {
                if (session != null && session.isOpen()) {
                    session.getAsyncRemote().sendText("ping", result -> logger.debug("ping status ", result));
                } else {
                    restart();
                }

                //5秒发送一次心跳
                Thread.sleep(invokerWebSocketProperties.getHeartbeat());
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    logger.warn("websocket {} connectionFactory has stopped listening", address);
                    return;
                }
                logger.warn("websocket {} connectionFactory container listening fail {}", address, ex.getMessage());
            }
        }
    }

    /**
     * 销毁
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        logger.warn("websocket {} rpc close", address);
        super.stop();
    }

    /**
     * 内部 Endpoint
     */
    private class InternalEndpoint extends Endpoint {
        @Override
        public void onOpen(Session session, EndpointConfig config) {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("webSocket {} open", address);
                }
                connectionMessageListenerList.forEach(openListener -> openListener.onOpen(session, config));
            } catch (Exception ex) {
                if (logger.isErrorEnabled()) {
                    logger.error("websocket open  ", ex);
                }
            }
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("webSocket {} close", address);
                }

                connectionMessageListenerList.forEach(closeListener -> closeListener.onClose(session, closeReason));
            } catch (Exception ex) {
                if (logger.isErrorEnabled()) {
                    logger.error("websocket close  ", ex);
                }
            }
        }

        @Override
        public void onError(Session session, Throwable thr) {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("webSocket {} error {}", address, thr);
                }

                connectionMessageListenerList.forEach(errorListener -> errorListener.onError(session, thr));
            } catch (Exception ex) {
                if (logger.isErrorEnabled()) {
                    logger.error("websocket error ", ex);
                }
            }
        }
    }
}
