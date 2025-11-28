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

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

import java.nio.ByteBuffer;

/**
 * @author liju.z
 * @date 2025/3/30 11:45
 */
public interface WebSocketConnectionMessageListener {
    /**
     * session 流消息
     * @param byteBuffer
     */
    void onMessage(ByteBuffer byteBuffer);

    /**
     * session 握手
     * @param session
     * @param config
     */
    void onOpen(Session session, EndpointConfig config);

    /**
     * session 错误
     * @param session
     * @param thr
     */
    void onError(Session session, Throwable thr);

    /**
     * session 关闭
     * @param session
     * @param closeReason
     */
    void onClose(Session session, CloseReason closeReason);
}
