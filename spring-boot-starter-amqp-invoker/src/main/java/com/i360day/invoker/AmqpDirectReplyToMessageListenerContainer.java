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

import com.i360day.invoker.support.AbstractMessageContainerSmartLifecycle;
import com.rabbitmq.client.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;

/**
 * @author liju.z
 * @date 2024/4/26 23:13
 */
public class AmqpDirectReplyToMessageListenerContainer extends AbstractMessageContainerSmartLifecycle {
    private final List<AmqpMessageListener> listenerList = new Vector<>();
    private final List<AmqpShutdownListener> amqpShutdownListenerList = new Vector<>();
    private final List<DirectReplySimpleConsumer> consumerList = new LinkedList<>();
    private RabbitResourceHolder resourceHolder;
    private Channel channel;
    private String consumerTag;
    private ConnectionFactory connectionFactory;

    /**
     * 根据每个不同连接创建
     * @param connectionFactory
     */
    public AmqpDirectReplyToMessageListenerContainer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        //添加默认队列
        addDefaultQueue(AmqpAddress.REPLY_QUEUE_KEY);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * 添加消息处理器
     * @param messageListener
     */
    public void addListener(AmqpMessageListener messageListener){
        this.listenerList.add(messageListener);
    }

    /**
     * 添加停止监听器
     * @param amqpShutdownListener
     */
    public void addShutdownListener(AmqpShutdownListener amqpShutdownListener){
        this.amqpShutdownListenerList.add(amqpShutdownListener);
    }

    /**
     * 添加队列
     * @param queues
     */
    public void addQueue(String ...queues){
        Assert.state(this.channel != null && this.channel.isOpen(), "channel is null or close，");
        addDefaultQueue(queues);
    }

    private void addDefaultQueue(String ...queues){
        Arrays.asList(queues).forEach(queue -> {
            DirectReplySimpleConsumer directReplySimpleConsumer = new DirectReplySimpleConsumer(channel, queue, false);
            this.consumerList.add(directReplySimpleConsumer);
        });
    }
    public Channel getChannel() {
        return channel;
    }

    public RabbitResourceHolder getResourceHolder() {
        return resourceHolder;
    }

    /**
     * 启动监听
     */
    @Override
    protected void doStart() {
        try{
            //创建通道
            this.resourceHolder = Optional.ofNullable(this.resourceHolder).orElseGet(() -> {
                return ConnectionFactoryUtils.getTransactionalResourceHolder(getConnectionFactory(), false);
            });
            this.channel = Optional.ofNullable(this.channel).orElseGet(() -> this.resourceHolder.getChannel());
            ClosingRecoveryListener.addRecoveryListenerIfNecessary(this.channel);

            //通道关闭后重置队列监听
            this.channel.addShutdownListener(e -> {
                //通道关闭后，通知监听器
                amqpShutdownListenerList.forEach(amqpShutdownListener -> amqpShutdownListener.channelShutdownCompleted(getConnectionFactory(), e));

                logger.warn("direct reply container fail " + e.getMessage());
            });

            //判断队列是否存在
            this.channel.queueDeclarePassive(AmqpAddress.REPLY_QUEUE_KEY);

            //监听消息回复队列
            DirectReplySimpleConsumer consumer = new DirectReplySimpleConsumer(this.channel, AmqpAddress.REPLY_QUEUE_KEY, false);
            this.consumerTag = this.channel.basicConsume(AmqpAddress.REPLY_QUEUE_KEY, true, consumer);
            consumer.setConsumerTag(this.consumerTag);

        }catch (Exception ex) {
            super.stop();
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 重置队列监听
     * @param consumer
     */
    private void restartConsumer(DirectReplySimpleConsumer consumer){
        //删除已存在的
        consumerList.remove(consumer);

        //添加新的
        consumerList.add(consumer);

        //重新开启监听
        doStart();
    }

    /**
     * 停止运行
     */
    @Override
    protected void doStop() {

        super.doStop();

        try{
            //清除队列
            consumerList.clear();

            try {
                //取消队列监听
                RabbitUtils.closeMessageConsumer(this.channel, Arrays.asList(this.consumerTag), true);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error closing consumer " + this, e);
                }
            }

            //关闭连接
            RabbitUtils.setPhysicalCloseRequired(this.channel, true);
            ConnectionFactoryUtils.releaseResources(this.resourceHolder);
        }catch (Exception ex){
            //ignore ex
        }
    }

    /**
     * 执行
     *
     * @param run
     * @return
     */
    public void execute(ExecuteNotCallback run) throws IOException {
        Channel channel = getChannel();
        if(channel == null || !channel.isOpen()){
            throw new IllegalCallerException("channel is null or close");
        }
        run.call(channel);
    }

    @Override
    public void afterPropertiesSet()  {
        Assert.notNull(this.connectionFactory, "ConnectionFactory is required");
    }

    public interface ExecuteCallback<T>{
        T call(Channel channel) throws IOException;
    }

    public interface ExecuteNotCallback<T>{
        void call(Channel channel) throws IOException;
    }

    /**
     * 添加消息回复队列
     */
    private class DirectReplySimpleConsumer extends DefaultConsumer {
        private String consumerTag;
        private final String queue;
        private final boolean ackRequired;

        public DirectReplySimpleConsumer(Channel channel, String queue, boolean ackRequired) {
            super(channel);
            this.queue = queue;
            this.ackRequired = ackRequired;
        }

        public boolean isAckRequired() {
            return ackRequired;
        }

        public String getQueue() {
            return queue;
        }

        public void setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
        }

        @Override
        public String getConsumerTag() {
            return consumerTag;
        }

        /**
         * 消息取消
         * @param consumerTag the defined consumer tag (client- or server-generated)
         * @throws IOException
         */
        @Override
        public void handleCancel(String consumerTag) throws IOException {
            listenerList.forEach(item -> item.handleCancel(getConnectionFactory(), consumerTag, null));
        }

        /**
         * 队列收到消息
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         * @param envelope packaging data for the message
         * @param properties content header data for the message
         * @param body the message body (opaque, client-specific byte array)
         * @throws IOException
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            try{
                for (AmqpMessageListener amqpMessageListener : listenerList) {
                    amqpMessageListener.handleDelivery(channel, consumerTag, envelope, properties, body);
                }
            }catch (Exception ex){
                if(logger.isDebugEnabled()){
                    logger.error("amqp Message received successfully, processing failed. Please check for malicious requests ");
                }
                listenerList.forEach(item -> item.handleCancel(getConnectionFactory(), consumerTag, ex));
            }
        }

        /**
         * 队列停止监听
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         * @param sig a {@link ShutdownSignalException} indicating the reason for the shut down
         */
        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            listenerList.forEach(item -> item.handleShutdownSignal(getConnectionFactory(), consumerTag, sig));
        }
    }
}
