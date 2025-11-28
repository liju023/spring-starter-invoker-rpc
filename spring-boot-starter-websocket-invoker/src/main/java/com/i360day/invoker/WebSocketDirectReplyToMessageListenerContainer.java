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
package com.i360day.invoker;

import com.i360day.invoker.codes.WebSocketMessageSerialize;
import com.i360day.invoker.socket.connection.WebSocketConnectionFactory;
import com.i360day.invoker.socket.connection.WebSocketConnectionMessageListener;
import com.sun.jdi.connect.spi.ClosedConnectionException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Buffers;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

/**
 * @author liju.z
 * @date 2025/3/29 10:54
 */
public class WebSocketDirectReplyToMessageListenerContainer implements WebSocketConnectionMessageListener {
    private final Logger logger = LoggerFactory.getLogger(WebSocketDirectReplyToMessageListenerContainer.class);
    private List<WebSocketReplyToMessageListener> messageListenerList = new Vector<>();
    private WebSocketConnectionFactory webSocketConnectionFactory;
    private WebSocketMessageSerialize webSocketMessageSerialize;

    /**
     * @param webSocketConnectionFactory
     */
    public WebSocketDirectReplyToMessageListenerContainer(WebSocketMessageSerialize webSocketMessageSerialize, WebSocketConnectionFactory webSocketConnectionFactory) {
        this.webSocketMessageSerialize = webSocketMessageSerialize;
        this.webSocketConnectionFactory = webSocketConnectionFactory;
        this.webSocketConnectionFactory.addConnectionMessageListener(this);
    }

    public WebSocketConnectionFactory getWebSocketConnectionFactory() {
        return webSocketConnectionFactory;
    }

    /**
     * 添加消息处理器
     *
     * @param messageListener
     */
    public void addListener(WebSocketReplyToMessageListener messageListener) {
        this.messageListenerList.add(messageListener);
    }

    /**
     * 执行
     *
     * @param run
     * @return
     */
    public <T> T execute(ExecuteNotCallback run) throws Exception {
        Session session = webSocketConnectionFactory.getSession();
        if (session == null) {
            throw new IllegalCallerException("Session connection is null To initialize");
        } else if (!session.isOpen()) {
            throw new ClosedConnectionException("Session connection is close");
        }
        return (T) run.call(session);
    }

    /**
     * 消息回复处理
     *
     * @param byteBuffer
     */
    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        try {
            byte[] body = Buffers.take(byteBuffer).clone();
            WebSocketResponse webSocketResponse = webSocketMessageSerialize.decode(body, WebSocketResponse.class);
            for (WebSocketReplyToMessageListener replyToMessageListener : messageListenerList) {
                replyToMessageListener.handleDelivery(webSocketResponse);
            }
        } catch (Exception ex) {
            logger.warn("webSocket rpc message processing failed {}", ex);
            this.onError(webSocketConnectionFactory.getSession(), ex);
        }
    }

    /**
     * 链接成功处理
     *
     * @param session
     * @param config
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        try {
//            for (WebSocketReplyToMessageListener replyToMessageListener : messageListenerList) {
//                replyToMessageListener.handleDelivery();
//            }
        } catch (Exception ex) {
            logger.warn("webSocket open failed {}", ex);
            this.onError(webSocketConnectionFactory.getSession(), ex);
        }
    }

    /**
     * 消息失败
     *
     * @param session
     * @param thr
     */
    @Override
    public void onError(Session session, Throwable thr) {
        try {
            for (WebSocketReplyToMessageListener replyToMessageListener : messageListenerList) {
                replyToMessageListener.handleCancel(webSocketConnectionFactory, thr);
            }
        } catch (Exception ex) {
            logger.warn("webSocket rpc message processing failed {}", ex);
        }
    }

    /**
     * session关闭
     *
     * @param session
     * @param closeReason
     */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        try {
            for (WebSocketReplyToMessageListener replyToMessageListener : messageListenerList) {
                replyToMessageListener.handleShutdownSignal(webSocketConnectionFactory, closeReason);
            }
        } catch (Exception ex) {
            logger.warn("webSocket rpc close processing failed {}", ex);
        }
    }


    /**
     * 执行回调
     *
     * @param <T>
     */
    public interface ExecuteNotCallback<T> {
        T call(Session session) throws Exception;
    }
}
