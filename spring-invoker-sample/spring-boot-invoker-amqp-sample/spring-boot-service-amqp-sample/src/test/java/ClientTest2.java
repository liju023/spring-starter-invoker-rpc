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

import cn.hutool.core.io.FileUtil;
import com.i360day.invoker.AmqpAddress;
import com.i360day.invoker.common.InvokerConstant;
import com.rabbitmq.client.*;
import org.springframework.amqp.core.Queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liju.z
 * @date 2024/3/27 21:08
 */
public class ClientTest2 {

    private static AMQP.BasicProperties cloneBasicProperties(AMQP.BasicProperties properties) {
        return new AMQP.BasicProperties.Builder()
                .correlationId(properties.getCorrelationId())
                .replyTo(properties.getReplyTo())
                .headers(properties.getHeaders())
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

    public static void main(String[] args) throws IOException, TimeoutException, ExecutionException, InterruptedException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPassword("Ryb@2021");
        connectionFactory.setUsername("ryb");
        connectionFactory.setPort(25672);
        connectionFactory.setHost("47.92.170.164");
        connectionFactory.setVirtualHost("/ryb-data");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        CompletableFuture future = new CompletableFuture();
        //声明队列
        Queue queue = new Queue("amq.rabbitmq.reply-to", true, false, false, new HashMap<>());
        channel.queueDeclarePassive(queue.getName());
        channel.basicConsume(queue.getName(), true, "1", new DeliverCallback() {
            @Override
            public void handle(String s, Delivery delivery) throws IOException {
                System.out.println(new String(delivery.getBody()));
                future.complete(new String(delivery.getBody()));
            }
        }, new CancelCallback() {
            @Override
            public void handle(String s) throws IOException {
                System.out.println("取消" + s);
                future.cancel(true);
            }
        }, new ConsumerShutdownSignalCallback() {
            @Override
            public void handleShutdownSignal(String s, ShutdownSignalException e) {
                System.out.println("销毁" + s);
                future.completeExceptionally(e);
            }
        });

        AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.Builder()
                .correlationId(UUID.randomUUID().toString())
                .replyTo(queue.getName())
//                .headers(requestTemplate.getHeaders().keySet().stream().collect(Collectors.toMap(k -> k, v -> requestTemplate.getHeaders(v))))
                .contentType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                .contentEncoding("UTF-8")
                .messageId(UUID.randomUUID().toString())
//                            .appId(properties.getAppId())
//                            .clusterId(properties.getClusterId())
//                            .deliveryMode(properties.getDeliveryMode())
//                            .type(properties.getType())
//                            .userId(properties.getUserId())
                .build();
        byte[] bodys = FileUtil.readBytes("E:/liju.z/test/rpc/test.dat");
        channel.basicPublish(AmqpAddress.EXCHANGE_KEY, "/remote/server/springInvokerSampleServiceFacade", replyProperties, bodys);

        Object o = future.get(1000 * 30, TimeUnit.MILLISECONDS);
        System.out.println(o);

        if (connection.isOpen()) connection.close();
        if (channel.isOpen()) channel.close();
    }
}
