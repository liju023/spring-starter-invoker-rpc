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

import com.i360day.invoker.codes.RedisMessageSerialize;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.exception.InvokerException;
import com.i360day.invoker.exception.InvokerTimeoutException;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.properties.ModeType;
import com.i360day.invoker.properties.RedisInvokerProperties;
import com.i360day.invoker.request.InvokerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * redis httpInvoker 请求
 *
 * @author: liju.z
 * @date: 2024/3/18 13:38
 */
public class RedisInvokerRequest extends RedisInvokerSupportAbstractRequest implements InvokerRequest, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(RedisInvokerRequest.class);
    private final InvokerProperties invokerProperties;
    private final RedisInvokerProperties redisInvokerProperties;
    private final RedisMessageSerialize redisMessageSerialize;
    private final String RPC_REPLY_KEY = "redis.invoker.rpc.reply-to.%s-%s";
    private final Map<String, MessagePendingReply<RedisResponse>> replyHolder = new ConcurrentHashMap<>();
    private final Map<String, RedisConnectionFactory> multiTenancyConnectionFactory = new LinkedHashMap<>();
    private final Map<RedisConnectionFactory, RedisDirectReplyToMessageListenerContainer> replyToMessageListenerContainer = new ConcurrentHashMap<>();

    /**
     * create redis request
     *
     * @param redisConnectionFactory
     * @param invokerProperties
     */
    public RedisInvokerRequest(RedisConnectionFactory redisConnectionFactory,
                               InvokerProperties invokerProperties,
                               RedisMessageSerialize redisMessageSerialize,
                               RedisInvokerProperties redisInvokerProperties) {
        super(redisConnectionFactory);
        this.invokerProperties = invokerProperties;
        this.redisMessageSerialize = redisMessageSerialize;
        this.redisInvokerProperties = redisInvokerProperties;
    }

    /**
     * 创建消息回复容器
     *
     * @param redisConnectionFactory
     * @return
     */
    private RedisDirectReplyToMessageListenerContainer createListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisDirectReplyToMessageListenerContainer messageListenerContainer = new RedisDirectReplyToMessageListenerContainer(
                String.format(RPC_REPLY_KEY, System.currentTimeMillis(), UUID.randomUUID()),
                redisConnectionFactory,
                this.redisMessageSerialize,
                redisInvokerProperties.getModeType()
        );
        messageListenerContainer.addListener(this);
        messageListenerContainer.afterPropertiesSet();
        messageListenerContainer.start();
        return messageListenerContainer;
    }

    /**
     * 执行rpc请求
     *
     * @param requestTemplate
     * @return
     * @throws IOException
     */
    @Override
    public Response executor(RequestTemplate requestTemplate) throws IOException {
        RedisConnectionFactory redisConnectionFactory = getRedisConnectionFactory(requestTemplate);

        //创建订阅消息回复容器
        RedisDirectReplyToMessageListenerContainer redisDirectReplyToMessageListenerContainer = this.replyToMessageListenerContainer.get(redisConnectionFactory);
        if (redisDirectReplyToMessageListenerContainer == null) {
            synchronized (this.replyToMessageListenerContainer) {
                redisDirectReplyToMessageListenerContainer = this.replyToMessageListenerContainer.get(redisConnectionFactory);
                if (redisDirectReplyToMessageListenerContainer == null) {
                    redisDirectReplyToMessageListenerContainer = createListenerContainer(redisConnectionFactory);
                    this.replyToMessageListenerContainer.put(redisConnectionFactory, redisDirectReplyToMessageListenerContainer);
                }
            }
        }

        return sendMessage(requestTemplate, redisDirectReplyToMessageListenerContainer);
    }

    /**
     * 发送请求订阅消息
     *
     * @param requestTemplate
     * @param replyToMessageListenerContainer
     * @return
     */
    private Response sendMessage(RequestTemplate requestTemplate, RedisDirectReplyToMessageListenerContainer replyToMessageListenerContainer) {
        //request
        RedisRequest redisRequest = RedisRequest.convertRedisRequest(replyToMessageListenerContainer.getRedisQueueReplyTo(), requestTemplate);
        redisRequest.setReadTimeout(invokerProperties.getRequest().getReadTimeout());

        //消息回复容器
        MessagePendingReply<RedisResponse> messagePendingReply = replyHolder.computeIfAbsent(redisRequest.getRequestId(), (o) -> {
            return new MessagePendingReply<>(replyToMessageListenerContainer.getRedisConnection().toString());
        });

        try {
            ModeType modeType = ModeType.valueOf(requestTemplate.getTargetProxy().getAnnotationAttributeAsString("modeType"));
            if(ModeType.UNKNOWN.equals(modeType)){
                modeType = redisInvokerProperties.getModeType();
            }
            //主动模式，lpush队列
            if (ModeType.ACTIVE.equals(modeType)) {
                int expireTime = invokerProperties.getRequest().getReadTimeout() + 1000;

                return replyToMessageListenerContainer.sendQueueMessage(requestTemplate.getRequestPath().getBytes(), redisMessageSerialize.encode(redisRequest), expireTime, (redisConnection -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("send message -> routing key = {}", requestTemplate.getRequestPath());
                    }
                    try {
                        //返回信息
                        RedisResponse redisResponse = messagePendingReply.get(expireTime, TimeUnit.MILLISECONDS);

                        return Response.Builder.create()
                                .body(new ByteArrayInputStream(redisResponse.getBody()))
                                .status(redisResponse.getHttpStatus().value())
                                .setContextType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                                .build();
                    } finally {
                        //删除key
                        redisConnection.del(requestTemplate.getRequestPath().getBytes());
                    }
                }));
            }
            //被动模式，订阅消息
            else {
                return replyToMessageListenerContainer.subscribe(requestTemplate.getRequestPath().getBytes(), redisMessageSerialize.encode(redisRequest), (redisConnection) -> {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("send message -> routing key = {}", requestTemplate.getRequestPath());
                        }
                        //返回信息
                        RedisResponse redisResponse = messagePendingReply.get(invokerProperties.getRequest().getReadTimeout(), TimeUnit.MILLISECONDS);

                        return Response.Builder.create()
                                .body(new ByteArrayInputStream(redisResponse.getBody()))
                                .status(redisResponse.getHttpStatus().value())
                                .setContextType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                                .build();
                    } finally {
                        //删除key
                        redisConnection.del(requestTemplate.getRequestPath().getBytes());
                    }
                });
            }
        } catch (Exception ex) {
            if (ex instanceof TimeoutException) {
                throw new InvokerTimeoutException(ex);
            }
            throw new InvokerException(ex);
        } finally {
            replyHolder.remove(redisRequest.getRequestId());
        }
    }

    /**
     * 处理redis订阅回复消息
     *
     * @param keys
     * @param redisResponse
     */
    @Override
    public void handleDelivery(String keys, RedisResponse redisResponse) {
        try {
            Optional.ofNullable(replyHolder.get(redisResponse.getRequestId())).ifPresent(o -> {
                o.reply(redisResponse);
            });
        } catch (Exception ex) {
            logger.warn("redis request Subscription message processing failed {}", ex.getMessage());
        }
    }

    /**
     * 订阅请求容器发生错误
     *
     * @param connectionFactory
     * @param sig
     */
    @Override
    public void handleShutdownSignal(RedisConnectionFactory connectionFactory, Exception sig) {
        replyHolder.values().stream()
                .filter(f -> f.getContainerId().equals(connectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(sig));
    }

    /**
     * 取消订阅请求
     *
     * @param connectionFactory
     * @param exception
     */
    @Override
    public void handleCancel(RedisConnectionFactory connectionFactory, Exception exception) {
        replyHolder.values().stream()
                .filter(f -> f.getContainerId().equals(connectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(exception));
    }

    /**
     * 销毁
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        //回复消息容器
        replyToMessageListenerContainer.values().forEach(container -> container.stop());

        //多维度链接工厂
        multiTenancyConnectionFactory.values().forEach(connectionFactory -> {
            if (connectionFactory instanceof SmartLifecycle smartLifecycle) {
                try {
                    smartLifecycle.stop();
                } catch (Exception ex) {
                    //ignore ex
                }
            }
        });

        //消息回复
        replyHolder.values().forEach(replyHolder -> replyHolder.completeExceptionally(new InterruptedException()));
    }
}
