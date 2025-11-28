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
import com.i360day.invoker.support.RemoteInvocation;
import com.i360day.invoker.support.RemoteInvocationFactory;
import com.i360day.invoker.support.RemoteInvocationResult;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author: liju.z
 * @date: 2024/3/20 11:08
 */
public class AmqpInvokerServiceExporter extends HttpInvokerServiceExporter implements InitializingBean, AmqpMessageListener {

    /**
     * constructor
     *
     * @param remoteInvocationFactory
     */
    public AmqpInvokerServiceExporter(RemoteInvocationFactory remoteInvocationFactory) {
        super(remoteInvocationFactory);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }

    /**
     * clone amqp properties
     *
     * @param properties
     * @return
     */
    private AMQP.BasicProperties cloneBasicProperties(AMQP.BasicProperties properties) {
        return new AMQP.BasicProperties.Builder()
                .correlationId(properties.getCorrelationId())
                .replyTo(properties.getReplyTo())
                .headers(Optional.ofNullable(properties.getHeaders()).orElse(new HashMap<>()))
                .contentType(properties.getContentType())
                .appId(properties.getAppId())
                .clusterId(properties.getClusterId())
                .deliveryMode(properties.getDeliveryMode())
                .contentEncoding(properties.getContentEncoding())
                .messageId(properties.getMessageId())
                .type(properties.getType())
                .userId(properties.getUserId())
                .build();
    }

    /**
     * 消息处理
     *
     * @param channel
     * @param consumerTag
     * @param envelope
     * @param properties
     * @param body
     * @throws IOException
     */
    @Override
    public void handleDelivery(Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        //返回配置consumerTag, message
        AMQP.BasicProperties replyProperties = cloneBasicProperties(properties);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            //verify user
            verifyUser(Optional.ofNullable(replyProperties.getHeaders()).orElse(Collections.emptyMap()).get(InvokerConstant.AUTHORIZATION) + "");

            //analysis
            RemoteInvocation invocation = doReadRemoteInvocation(new ByteArrayInputStream(body));
            Type returnType = invocation.getReturnType(getService());

            //invoke
            RemoteInvocationResult result = invokeAndCreateResult(invocation, getProxy());
            getEncoder().encoder(outputStream, returnType == null ? Object.class : returnType, result);
        } catch (Exception ex) {
            //ignore exception
            logger.warn("rabbitmq reply message error " + ex.getMessage());
            if(ex instanceof RemoteClientUserException){
                outputStream.write(-2);
            }else{
                outputStream.write(-1);
            }
        }
        //result
        Assert.hasText(properties.getReplyTo(), "amqp replyTo is null, Unable to notify queue");
        channel.basicPublish("", properties.getReplyTo(), replyProperties, outputStream.toByteArray());
    }
}
