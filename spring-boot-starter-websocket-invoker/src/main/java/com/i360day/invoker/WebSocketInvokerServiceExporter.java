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
package com.i360day.invoker;

import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.exception.RemoteClientUserException;
import com.i360day.invoker.socket.handler.InvokerMessageByteBufferHandler;
import com.i360day.invoker.support.RemoteInvocation;
import com.i360day.invoker.support.RemoteInvocationFactory;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * url -> service handler
 *
 * @author: liju.z
 * @date: 2024/3/20 11:08
 */
public class WebSocketInvokerServiceExporter extends HttpInvokerServiceExporter implements WebSocketMessageListener {

    /**
     * WebSocketInvokerServiceExporter
     *
     * @param remoteInvocationFactory
     * @see com.i360day.invoker.annotation.RemoteWebSocketClient#serverProxyClass
     */
    public WebSocketInvokerServiceExporter(RemoteInvocationFactory remoteInvocationFactory) {
        super(remoteInvocationFactory);
    }

    /**
     * webSocket 消息处理
     *
     * @param webSocketRequest
     * @return
     * @see InvokerMessageByteBufferHandler#onMessage
     */
    @Override
    public WebSocketResponse handleDelivery(WebSocketRequest webSocketRequest) {
        WebSocketResponse.Buildler responseBuilder = WebSocketResponse.builder()
                .requestId(webSocketRequest.getRequestId())
                .replyTo(webSocketRequest.getReplyTo())
                .responseId(webSocketRequest.getRequestId())
                .httpStatus(HttpStatus.OK);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            //verify user
            verifyUser(webSocketRequest.getHeader(InvokerConstant.AUTHORIZATION));

            //analysis
            RemoteInvocation invocation = doReadRemoteInvocation(new ByteArrayInputStream(webSocketRequest.getBody()));
            Type returnType = invocation.getReturnType(getService());

            //invoke
            getEncoder().encoder(outputStream, returnType == null ? Object.class : returnType, invokeAndCreateResult(invocation, getProxy()));

            //result
            return responseBuilder.body(outputStream.toByteArray()).build();

        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.error("websocket rpc server execute fail", ex);
            }

            if (ex instanceof RemoteClientUserException) {
                responseBuilder.httpStatus(HttpStatus.FORBIDDEN);
            }

            //result
            return responseBuilder
                    .body(ex.getMessage().getBytes(StandardCharsets.UTF_8))
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
