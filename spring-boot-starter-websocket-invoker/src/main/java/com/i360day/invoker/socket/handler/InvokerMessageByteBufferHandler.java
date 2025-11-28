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
package com.i360day.invoker.socket.handler;

import com.i360day.invoker.WebSocketMessageListener;
import com.i360day.invoker.WebSocketRequest;
import com.i360day.invoker.WebSocketResponse;
import com.i360day.invoker.codes.WebSocketMessageSerialize;
import javax.websocket.Endpoint;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.xnio.Buffers;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * byteBuffer 消息处理
 * @author liju.z
 * @date 2024/4/10 21:13
 */
public class InvokerMessageByteBufferHandler implements MessageHandler.Whole<ByteBuffer> {
    /**
     * self session
     */
    private Session session;
    /**
     * endpoint
     */
    private Endpoint endpoint;
    private WebSocketMessageSerialize webSocketMessageSerialize;
    /**
     * url -> messageList
     */
    private final Map<String, WebSocketMessageListener> urlMessageHandlerMap;

    /**
     * byteBuffer 消息处理
     * @see InvokerWebSocketEndpoint#onOpen
     * @param endpoint
     * @param session
     * @param urlMessageHandlerMap
     */
    public InvokerMessageByteBufferHandler(Endpoint endpoint, Session session, WebSocketMessageSerialize webSocketMessageSerialize, Map<String, WebSocketMessageListener> urlMessageHandlerMap) {
        this.endpoint = endpoint;
        this.session = session;
        this.webSocketMessageSerialize = webSocketMessageSerialize;
        this.urlMessageHandlerMap = urlMessageHandlerMap;
    }

    /**
     * byteBuffer 消息
     * @param byteBuffer the message data.
     */
    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        try {
            byte[] body = Buffers.take(byteBuffer).clone();
            WebSocketRequest webSocketRequest = webSocketMessageSerialize.decode(body, WebSocketRequest.class);

            WebSocketMessageListener messageListener = this.urlMessageHandlerMap.get(webSocketRequest.getRequestId());
            if(messageListener == null) return;

            WebSocketResponse response = messageListener.handleDelivery(webSocketRequest);

            //回复消息
            session.getAsyncRemote().sendBinary(ByteBuffer.wrap(webSocketMessageSerialize.encode(response)), result -> {
                if (!result.isOK()) {
                    endpoint.onError(session, result.getException());
                }
            });


        } catch (Exception ex) {
            this.endpoint.onError(session, ex);
        }
    }
}
