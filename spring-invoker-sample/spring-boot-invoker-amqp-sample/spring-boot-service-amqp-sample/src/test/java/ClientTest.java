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
package com.boot.invoker.sample.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liju.z
 * @date 2024/3/24 22:07
 */
public class ClientTest {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPassword("Ryb@2021");
        connectionFactory.setUsername("ryb");
        connectionFactory.setPort(25672);
        connectionFactory.setHost("47.92.170.164");
        connectionFactory.setVirtualHost("/ryb-data");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        //声明交换机
        Exchange exchange = new TopicExchange("exchangeKey", true, false, new HashMap<>());
        //声明队列
        Queue queue = new Queue("queueKey", true, true, false, new HashMap<>());
        //绑定
        Binding binding = BindingBuilder.bind(queue).to(exchange).with("routingKey").and(new HashMap<>() {
            {
                //10分钟消息过期
                put("x-message-ttl", TimeUnit.MINUTES.toMillis(1));
            }
        });
//        AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclare(exchange.getName(), exchange.getType(), exchange.isDurable(), exchange.isAutoDelete(), exchange.isInternal(), exchange.getArguments());
//        AMQP.Queue.DeclareOk declareOk1 = channel.queueDeclare(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete(), queue.getArguments());
        DefaultMessagePropertiesConverter defaultMessagePropertiesConverter = new DefaultMessagePropertiesConverter();
        AMQP.BasicProperties basicProperties = defaultMessagePropertiesConverter.fromMessageProperties(new MessageProperties(), "UTF-8");
        channel.basicPublish("exchangeKey", "routingKey", null, "ok".getBytes());
    }
}
