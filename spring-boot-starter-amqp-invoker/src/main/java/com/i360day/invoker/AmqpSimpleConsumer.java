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

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpAuthenticationException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liju.z
 * @date 2024/6/19 21:43
 */
class AmqpSimpleConsumer {
    private final Logger logger = LoggerFactory.getLogger(AmqpSimpleConsumer.class);
    private final Lock lifecycleLock = new ReentrantLock();
    private String consumerTag;
    private final String queue;
    private final boolean ackRequired;
    private AmqpMessageListener amqpMessageListener;
    private Channel channel;
    private volatile boolean cancelled = false;
    private volatile boolean running = false;
    private volatile ShutdownSignalException shutdown;
    private Thread thread;
    private RabbitResourceHolder resourceHolder;
    private Binding binding;
    private ConnectionFactory connectionFactory;
    private int prefetchCount;
    private BlockingQueue<Boolean> messageResult;

    /**
     * 创建内部队列
     *
     * @param connectionFactory 链接工厂
     * @param binding           绑定队列
     * @param ackRequired       ack确认
     * @param prefetchCount     每个队列最大消费个数
     */
    public AmqpSimpleConsumer(ConnectionFactory connectionFactory, Binding binding, boolean ackRequired, int prefetchCount) {
        this.binding = binding;
        this.queue = binding.getDestination();
        this.ackRequired = ackRequired;
        this.connectionFactory = connectionFactory;
        this.prefetchCount = prefetchCount;
        this.messageResult = new LinkedBlockingQueue(prefetchCount);
    }

    public Binding getBinding() {
        return binding;
    }

    public String getQueue() {
        return queue;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public AmqpMessageListener getAmqpMessageListener() {
        return amqpMessageListener;
    }

    public void setAmqpMessageListener(AmqpMessageListener amqpMessageListener) {
        this.amqpMessageListener = amqpMessageListener;
    }

    /**
     * 获取通道
     * @return
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * 检查队列活跃
     * @return
     */
    public boolean checkRunning() {
        return this.running && !this.cancelled && shutdown == null;
    }

    public boolean isRunning() {
        return running;
    }

    public Boolean getMessageResult(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return messageResult.poll(timeout, timeUnit);
    }

    /**
     * 注册队列
     */
    public void start() {
        try {
            //给每个队列创建一个通道
            this.resourceHolder = ConnectionFactoryUtils.getTransactionalResourceHolder(connectionFactory, true);
            ClosingRecoveryListener.addRecoveryListenerIfNecessary(this.channel = this.resourceHolder.getChannel());

            //设置队列消费个数
            this.thread = Thread.currentThread();
            getChannel().basicQos(this.prefetchCount, false);

            //声明队列
            Exchange exchange = new TopicExchange(binding.getExchange(), true, true, new HashMap<>());
            Queue queue = new Queue(binding.getDestination(), false, false, true, new HashMap<>());
            AMQP.Queue.DeclareOk declareOk = queueDeclare(queue);
            if (declareOk != null) {
                this.consumerTag = declareOk.getQueue();

                //绑定交换机
                exchangeDeclare(exchange);

                //绑定队列
                queueBinding(binding);

                //监听队列
                InternalSimpleConsumer internalSimpleConsumer = new InternalSimpleConsumer(getChannel(), amqpMessageListener);
                String consumerTag = getChannel().basicConsume(this.queue, this.ackRequired, internalSimpleConsumer);
                internalSimpleConsumer.setConsumerTag(consumerTag);
            }
        }catch (Throwable ex) {
            if(ex instanceof AmqpAuthenticationException){
                logger.warn("Authentication failure {}", ex.getMessage());
            }else{
                logger.warn("amqp consumer fail {}", ex.getMessage());
            }
            this.running = false;
        }
    }

    /**
     * 绑定队列
     *
     * @param binding
     * @return
     */
    private AMQP.Queue.BindOk queueBinding(Binding binding) {
        try {
            return channel.queueBind(binding.getDestination(), binding.getExchange(), binding.getRoutingKey(), binding.getArguments());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 注册交换机
     *
     * @param exchange
     * @return
     */
    private AMQP.Exchange.DeclareOk exchangeDeclare(Exchange exchange) {
        try {
            return channel.exchangeDeclare(exchange.getName(), exchange.getType(), exchange.isDurable(), exchange.isAutoDelete(), exchange.isInternal(), exchange.getArguments());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 队列是否声明
     *
     * @param queue
     * @return          false 不能声明 true 可以声明
     */
    private boolean queueDeclarePassive(Queue queue) {
        try {
            channel.queueDeclarePassive(queue.getName());
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 注册队列
     *
     * @param queue
     * @return
     */
    private AMQP.Queue.DeclareOk queueDeclare(Queue queue) {
        try {
            if (queueDeclarePassive(queue)) {
                return channel.queueDeclare(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete(), queue.getArguments());
            }
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 停止队列
     */
    public void stop() {
        lifecycleLock.lock();
        try {
            if (!this.isCancelled()) {
                try {
                    RabbitUtils.closeMessageConsumer(getChannel(), Collections.emptySet(), true);
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Error closing consumer " + this, e);
                    }
                }
            }
            this.shutdown = null;
            this.cancelled = true;
            this.running = false;

            if (logger.isDebugEnabled()) {
                logger.debug("Closing Rabbit Channel: " + getChannel());
            }

            forceCloseAndClearQueue();
        } finally {
            this.lifecycleLock.unlock();
        }
    }

    public void forceCloseAndClearQueue() {
        if (getChannel() != null) {
            RabbitUtils.setPhysicalCloseRequired(getChannel(), true);
            ConnectionFactoryUtils.releaseResources(this.resourceHolder);
        }
    }

    /**
     * 内部使用队列
     */
    private class InternalSimpleConsumer extends DefaultConsumer{
        private String consumerTag;
        private AmqpMessageListener amqpMessageListener;
        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        public InternalSimpleConsumer(Channel channel, AmqpMessageListener amqpMessageListener) {
            super(channel);
            this.amqpMessageListener = amqpMessageListener;
        }

        @Override
        public String getConsumerTag() {
            return consumerTag;
        }

        public void setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
        }

        /**
         * 队列监听成功
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         */
        @Override
        public void handleConsumeOk(String consumerTag) {
            if(logger.isDebugEnabled()){
                logger.debug("队列 : {} 监听成功", consumerTag);
            }
            cancelled = false;
            running = true;
        }

        /**
         * 队列取消成功
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         */
        @Override
        public void handleCancelOk(String consumerTag) {
            if(logger.isDebugEnabled()){
                logger.debug("队列 : {} 已取消监听 成功", consumerTag);
            }
            cancelled = true;
        }

        /**
         * 队列取消
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         * @throws IOException
         */
        @Override
        public void handleCancel(String consumerTag) throws IOException {
            if(logger.isDebugEnabled()){
                logger.debug("队列 : {} 已取消监听  {}", consumerTag);
            }
            cancelled = true;
            amqpMessageListener.handleCancel(connectionFactory, consumerTag, null);
        }

        /**
         * 队列已关闭监听
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         * @param sig a {@link ShutdownSignalException} indicating the reason for the shut down
         */
        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            if(logger.isDebugEnabled()){
                logger.debug("队列 : {} 已停止监听", consumerTag, sig.getMessage());
            }
            shutdown = sig;
            amqpMessageListener.handleShutdownSignal(connectionFactory, consumerTag, sig);
        }

        /**
         * 队列消息处理
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         * @param envelope packaging data for the message
         * @param properties content header data for the message
         * @param body the message body (opaque, client-specific byte array)
         * @throws IOException
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            try{
                this.amqpMessageListener.handleDelivery(channel, consumerTag, envelope, properties, body);
            }catch (Exception ex){
                logger.warn("Received queue, processing failed. Please check for malicious calls {}", ex);
            }
        }
    }
}
