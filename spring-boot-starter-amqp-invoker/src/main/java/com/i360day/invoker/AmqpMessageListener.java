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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.io.IOException;

/**
 * @author liju.z
 * @date 2024/4/28 22:03
 */
public interface AmqpMessageListener {

    /**
     * 队列接收到消息处理
     * @param channel
     * @param consumerTag
     * @param envelope
     * @param properties
     * @param body
     * @throws IOException
     */
    void handleDelivery(Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException;

    /**
     * 队列关闭监听
     * @param consumerTag
     * @param sig
     */
    default void handleShutdownSignal(ConnectionFactory connectionFactory, String consumerTag, ShutdownSignalException sig){

    }

    /**
     * 队列取消执行
     * @param consumerTag
     * @param exception
     */
    default void handleCancel(ConnectionFactory connectionFactory, String consumerTag, Exception exception){

    }
}
