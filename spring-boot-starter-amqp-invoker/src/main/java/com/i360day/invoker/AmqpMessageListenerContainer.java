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

import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.support.AbstractMessageContainerSmartLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author liju.z
 * @date 2024/6/27 21:36
 */
public class AmqpMessageListenerContainer extends AbstractMessageContainerSmartLifecycle {
    /**
     * log
     */
    private final Logger logger = LoggerFactory.getLogger(AmqpMessageListenerContainer.class);
    /**
     * NOSONAR
     */
    private final Lock consumersLock = new ReentrantLock();
    /**
     * connection Factory
     */
    private ConnectionFactory connectionFactory;
    /**
     * properties
     */
    private InvokerProperties invokerProperties;
    /**
     * 每个线程最大能处理多少队列
     */
    private final int maxQueueSize;
    /**
     * 每个队列最大消费个数
     */
    private final int prefetchCount;

    /**
     * 创建
     *
     * @param connectionFactory
     * @param maxQueueSize
     * @param prefetchCount
     * @param invokerProperties
     */
    public AmqpMessageListenerContainer(ConnectionFactory connectionFactory, int maxQueueSize, int prefetchCount, InvokerProperties invokerProperties) {
        this.connectionFactory = connectionFactory;
        this.invokerProperties = invokerProperties;
        this.maxQueueSize = maxQueueSize;
        this.prefetchCount = prefetchCount;
    }

    /**
     * get ConnectionFactory
     *
     * @return
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * 当前bean创建完成执行
     */
    @Override
    public void doStart() {
        lifecycleLock.lock();
        try {
            //注册队列
            Map<String, AmqpInvokerServiceExporter> amqpInvokerServiceExporterMap = getApplicationContext().getBeansOfType(AmqpInvokerServiceExporter.class);
            List<AmqpSimpleConsumer> amqpSimpleConsumerList = amqpInvokerServiceExporterMap.keySet().stream().map((routingKey) -> {
                AmqpInvokerServiceExporter amqpInvokerServiceExporter = amqpInvokerServiceExporterMap.get(routingKey);

                //队列key
                String queueKey = String.format(AmqpAddress.AMQP_INVOKER_RPC_QUEUE, amqpInvokerServiceExporter.getServiceInterface().getSimpleName());

                //声明交换机
                Exchange exchange = new TopicExchange(AmqpAddress.EXCHANGE_KEY, true, true, new HashMap<>());

                //声明队列
                Queue queue = new Queue(queueKey, true, false, true, new HashMap<>());

                //绑定
                Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).and(new HashMap<>() {
                    {
                        put("x-message-ttl", TimeUnit.MINUTES.toMillis(invokerProperties.getRequest().getConnectionRequestTimeout()));
                    }
                });
                return createAmqpSimpleConsumer(binding, amqpInvokerServiceExporter);
            }).toList();

            //注册队列，每个线程可最大执行队列数
            if (!amqpSimpleConsumerList.isEmpty()) {

                //每个线程最多处理多少个队列
                int pageTotal = (amqpSimpleConsumerList.size() + maxQueueSize - 1) / maxQueueSize;
                IntStream.range(0, pageTotal).forEach(pageNum -> {
                    List<AmqpSimpleConsumer> consumerList = amqpSimpleConsumerList.stream().skip(pageNum).limit(maxQueueSize).toList();
                    getTaskExecutor().execute(new AsyncMessageProcessingConsumer(consumerList));
                });
            }
        } finally {
            lifecycleLock.unlock();
        }
    }

    /**
     * 创建队列
     *
     * @param binding
     * @param amqpMessageListener
     * @return
     */
    private AmqpSimpleConsumer createAmqpSimpleConsumer(Binding binding, AmqpMessageListener amqpMessageListener) {
        AmqpSimpleConsumer amqpSimpleConsumer = new AmqpSimpleConsumer(getConnectionFactory(), binding, true, this.prefetchCount);
        amqpSimpleConsumer.setAmqpMessageListener(amqpMessageListener);
        return amqpSimpleConsumer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.connectionFactory, "ConnectionFactory is required");
    }

    /**
     * 异步队列执行
     */
    private final class AsyncMessageProcessingConsumer implements Runnable {
        /**
         * 当前线程处理的队列列表
         */
        private List<AmqpSimpleConsumer> consumerList;

        /**
         * @param consumerList
         */
        public AsyncMessageProcessingConsumer(List<AmqpSimpleConsumer> consumerList) {
            this.consumerList = new Vector<>(consumerList);
        }

        /**
         * 获取队列
         *
         * @param amqpConsumer
         */
        private void forEachConsumer(Consumer<AmqpSimpleConsumer> amqpConsumer) {
            Iterator<AmqpSimpleConsumer> iterator = consumerList.iterator();
            while (iterator.hasNext()) {
                amqpConsumer.accept(iterator.next());
            }
        }

        /**
         * 指定队列是否活跃
         *
         * @param consumer
         * @return
         */
        private boolean isActive(AmqpSimpleConsumer consumer) {
            consumersLock.lock();
            try {
                return consumer.checkRunning() && this.consumerList != null && this.consumerList.contains(consumer);
            } finally {
                consumersLock.unlock();
            }
        }


        /**
         * 重置队列监听
         *
         * @param oldConsumer
         */
        private void restart(AmqpSimpleConsumer oldConsumer) {
            AmqpSimpleConsumer consumer = oldConsumer;
            consumersLock.lock();
            try {
                // Need to recycle the channel in this consumer
                consumer.stop();
                // Ensure consumer counts are correct (another is going
                // to start because of the exception, but
                // we haven't counted down yet)
                if (!isRunning()) {
                    // Do not restart - container is stopping
                    return;
                }
                consumer.start();
            } catch (RuntimeException ex) {
                logger.warn("Consumer failed irretrievably on restart. " + ex.getClass() + ": " + ex.getMessage());
            } finally {
                consumersLock.unlock();
            }
        }

        /**
         * 每个队列都有异步执行
         */
        @Override
        public void run() {

            while (isRunning()) {
                try {
                    mainLoop();
                    //线程等待1秒后执行
                    TimeUnit.MILLISECONDS.sleep(500L);
                } catch (Throwable ex) {
                    logger.warn("listener is error {}", ex.getMessage());

                    boolean interrupted = ex instanceof InterruptedException;
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                    killOrRestart(interrupted);
                }
            }
        }

        /**
         * 队列主入口
         */
        private void mainLoop() {
            forEachConsumer(amqpSimpleConsumer -> {
                try {
                    //如果没启动，则先启动
                    if (!amqpSimpleConsumer.isRunning()) {
                        amqpSimpleConsumer.start();
                        return;
                    }

                    //如果当前队列不活跃，则从新启动
                    if (!isActive(amqpSimpleConsumer)) {
                        restart(amqpSimpleConsumer);
                    }

                } catch (Exception ex) {
                    logger.warn("consumer restart error {}", ex.getMessage());
                }
            });
        }

        /**
         * 关闭队列并重置队列
         *
         * @param aborted 是否取消监听，如果是则不再次重置队列
         */
        private void killOrRestart(final boolean aborted) {
            forEachConsumer(amqpSimpleConsumer -> {
                try {
                    //停止
                    amqpSimpleConsumer.stop();

                    //是否取消监听
                    if (!aborted || isRunning()) {

                        //重置
                        amqpSimpleConsumer.start();
                    }
                } catch (Exception ex) {
                    logger.warn("rabbitmq kill or restart error {}", ex.getMessage());
                }
            });
        }
    }
}
