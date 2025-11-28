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
import com.i360day.invoker.exception.InvokerException;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.request.InvokerRequest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * amqp 请求处理
 *
 * @author: liju.z
 * @date: 2024/3/18 13:38
 */
public class AmqpInvokerRequest extends AmqpInvokerSupportAbstractRequest implements InvokerRequest, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(AmqpInvokerRequest.class);
    /**
     * 消息回复
     */
    private final Map<String, MessagePendingReply<byte[]>> replyHolder = new ConcurrentHashMap<>();
    /**
     * 链接工厂消息监听容器
     */
    private final Map<ConnectionFactory, AmqpDirectReplyToMessageListenerContainer> directReplyToContainers = new ConcurrentHashMap<>();

    /**
     * 构造
     *
     * @param invokerProperties
     */
    public AmqpInvokerRequest(InvokerProperties invokerProperties) {
        super(invokerProperties);
    }

    /**
     * 构造
     *
     * @param connectionFactory
     * @param invokerProperties
     */
    public AmqpInvokerRequest(ConnectionFactory connectionFactory, InvokerProperties invokerProperties) {
        super(connectionFactory, invokerProperties);
    }

    /**
     * 请求执行
     *
     * @param requestTemplate
     * @return
     * @throws IOException
     */
    @Override
    public Response executor(RequestTemplate requestTemplate) throws IOException {
        //根据RemoteAmqpClient中的指定rabbitmq创建连接工厂
        ConnectionFactory connectionFactory = getConnectionFactory(requestTemplate);

        //创建回复队列容器
        AmqpDirectReplyToMessageListenerContainer messageListenerContainer = directReplyToContainers.get(connectionFactory);
        if (messageListenerContainer == null) {
            synchronized (directReplyToContainers) {
                messageListenerContainer = directReplyToContainers.get(connectionFactory);
                if (messageListenerContainer == null) {
                    messageListenerContainer = createListenerContainer(connectionFactory);
                    directReplyToContainers.put(connectionFactory, messageListenerContainer);
                }
            }
        }

        Map<String, Object> headers = new HashMap<>();
        requestTemplate.getHeaders().forEach((key, values) -> {
            headers.put(key, values.stream().collect(Collectors.joining(",")));
        });

        AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.Builder()
                .correlationId(UUID.randomUUID().toString())
                .replyTo(AmqpAddress.REPLY_QUEUE_KEY)
                .headers(requestTemplate.getHeaders().keySet().stream().collect(Collectors.toMap(k -> k, v -> requestTemplate.getHeaders(v))))
                .contentType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                .contentEncoding(requestTemplate.getCharset().toString())
                .messageId(UUID.randomUUID().toString())
                .headers(headers)
//                            .appId(properties.getAppId())
//                            .clusterId(properties.getClusterId())
//                            .deliveryMode(properties.getDeliveryMode())
//                            .type(properties.getType())
//                            .userId(properties.getUserId())
                .build();


        return sendMessage(requestTemplate, messageListenerContainer, replyProperties);
    }

    /**
     * 发送队列
     *
     * @param requestTemplate          请求数据包
     * @param messageListenerContainer 队列消息回复容器
     * @param replyProperties          队列消息配置
     * @return Response                    返回数据包
     * @throws IOException
     */
    protected Response sendMessage(RequestTemplate requestTemplate, AmqpDirectReplyToMessageListenerContainer messageListenerContainer, AMQP.BasicProperties replyProperties) throws IOException {

        //异步消息等待答复
        MessagePendingReply<byte[]> messagePendingReply = this.replyHolder.computeIfAbsent(replyProperties.getCorrelationId(), (correlationId) -> {
            return new MessagePendingReply<>(messageListenerContainer.getConnectionFactory().toString());
        });

        try {
            //发送队列消息
            messageListenerContainer.execute(channel -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("send message -> exchange key = {} , routing key = {}", AmqpAddress.EXCHANGE_KEY, requestTemplate.getRequestPath());
                }
                channel.basicPublish(AmqpAddress.EXCHANGE_KEY, requestTemplate.getRequestPath(), replyProperties, requestTemplate.getBodyTemplate().getBody());
            });

            //获取数据包
            byte[] bytes = messagePendingReply.get(getHttpInvokerProperties().getRequest().getReadTimeout(), TimeUnit.MILLISECONDS);

            //返回信息
            return Response.Builder.create()
                    .body(new ByteArrayInputStream(bytes == null ? new byte[0] : bytes))
                    .status(bytes != null ? HttpStatus.OK.value() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setContextType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                    .build();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new InvokerException(e);
        } finally {
            this.replyHolder.remove(replyProperties.getCorrelationId());
        }
    }

    /**
     * bean 销毁
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        //停止所有容器
        directReplyToContainers.values().forEach(container -> container.stop());

        //清空数据
        directReplyToContainers.clear();

        getMultiTenancyConnectionFactory().values().forEach(connectionFactory -> {
            if (connectionFactory instanceof SmartLifecycle smartLifecycle) {
                try {
                    smartLifecycle.stop();
                } catch (Exception ex) {
                    //ignore ex
                }
            }
        });

        getMultiTenancyConnectionFactory().clear();

        //通知等待的线程
        replyHolder.values().forEach(replyHolder -> replyHolder.completeExceptionally(new InterruptedException()));
    }

    @Override
    public void handleDelivery(Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        Optional.ofNullable(replyHolder.get(properties.getCorrelationId())).ifPresent(o -> {
            if (body != null && body.length == 1) {
                //处理异常
                if(body[0] == -1){
                    o.completeExceptionally(new InvokerException("amqp server processing queue error"));
                }
                //认证异常
                else if(body[0] == -2){
                    o.completeExceptionally(new InvokerException("client auth error， Account or password mismatch"));
                }
                //默认
                else{
                    o.completeExceptionally(new InvokerException("Unable to identify message body"));
                }
            } else {
                o.reply(body);
            }
        });
    }

    /**
     * 队列取消
     *
     * @param consumerTag
     * @param exception
     */
    @Override
    public void handleCancel(ConnectionFactory connectionFactory, String consumerTag, Exception exception) {
        replyHolder.values().stream().
                filter(f -> f.getContainerId().equals(connectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(exception));
    }

    /**
     * 队列停止监听
     *
     * @param consumerTag
     * @param sig
     */
    @Override
    public void handleShutdownSignal(ConnectionFactory connectionFactory, String consumerTag, ShutdownSignalException sig) {
        replyHolder.values().stream()
                .filter(f -> f.getContainerId().equals(connectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(sig));
    }

    /**
     * channel关闭
     *
     * @param cause
     */
    @Override
    public void channelShutdownCompleted(ConnectionFactory connectionFactory, ShutdownSignalException cause) {

        //通知已在请求的队列
        replyHolder.values().stream()
                .filter(f -> f.getContainerId().equals(connectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(cause));

        //删除指定连接
        directReplyToContainers.remove(connectionFactory);
    }
}
