package com.i360day.invoker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.i360day.invoker.codes.RedisMessageSerialize;
import com.i360day.invoker.exception.RedisCloseException;
import com.i360day.invoker.exception.RedisMessageException;
import com.i360day.invoker.exception.RedisPingException;
import com.i360day.invoker.properties.ModeType;
import com.i360day.invoker.properties.RedisInvokerProperties;
import com.i360day.invoker.support.AbstractMessageContainerSmartLifecycle;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * redis 消息监听容器
 */
public class RedisMessageListenerContainer extends AbstractMessageContainerSmartLifecycle {
    /**
     * redis 链接工厂
     */
    private final RedisConnectionFactory redisConnectionFactory;
    /**
     * redis roperties
     */
    private final RedisInvokerProperties redisInvokerProperties;
    /**
     * redis message serialize
     */
    private final RedisMessageSerialize redisMessageSerialize;
    /**
     * 路由列表
     */
    private List<String> routingListenerList = new LinkedList<>();
    /**
     * 消息监听列表
     */
    private List<RedisSubscribeMessageListener> messageListenerList = new LinkedList<>();
    /**
     * redis消息处理监听
     */
    private Map<String, List<RedisSubscribeMessageListener>> messageListenerMap = new ConcurrentHashMap<>();

    /**
     * 每个线程最大能处理多少队列
     */
    private final int maxQueueSize;

    public RedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, RedisInvokerProperties redisInvokerProperties, RedisMessageSerialize redisMessageSerialize, int maxQueueSize) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisInvokerProperties = redisInvokerProperties;
        this.redisMessageSerialize = redisMessageSerialize;
        this.maxQueueSize = maxQueueSize;
    }

    public void addMessageListener(RedisSubscribeMessageListener... redisSubscribeMessageListeners) {
        this.messageListenerList.addAll(Arrays.asList(redisSubscribeMessageListeners));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.redisConnectionFactory, "redisConnection is required");
    }

    /**
     * 启动
     */
    @Override
    protected void doStart() {
        //获取队列
        Map<String, RedisInvokerServiceExporter> serviceExporterMap = getApplicationContext().getBeansOfType(RedisInvokerServiceExporter.class);

        //设置路由订阅监听
        this.routingListenerList.addAll(serviceExporterMap.keySet());

        //设置订阅消息处理回调
        serviceExporterMap.forEach((queue, listeners) -> messageListenerMap.put(queue, Arrays.asList(listeners)));

        //执行容器启动
        RedisConnection redisConnection = this.redisConnectionFactory.getConnection();

        //保存的key
        byte[][] bytes = routingListenerList.stream().map(m -> m.getBytes()).toArray(byte[][]::new);

        //主动模式，lpush队列
        if(ModeType.ACTIVE.equals(redisInvokerProperties.getModeType())){

            //每个线程最多处理多少个队列
            int pageTotal = (bytes.length + maxQueueSize - 1) / maxQueueSize;
            IntStream.range(0, pageTotal).forEach(pageNum -> {
                byte[][] keyBytes = IntStream.range(0, bytes.length).skip(pageNum).limit(maxQueueSize).mapToObj(i -> bytes[i]).toArray(byte[][]::new);
                this.getTaskExecutor().execute(new AsyncMessageActiveProcessing(redisConnection, keyBytes));
            });
        }
        //被动模式，订阅消息
        else{
            this.getTaskExecutor().execute(new AsyncMessagePassiveProcessing(redisConnection, bytes));
        }
    }

    /**
     * 停止
     */
    @Override
    protected void doStop() {
        super.doStop();
    }

    /**
     * 异步队列执行-被动模式
     */
    private class AsyncMessagePassiveProcessing implements Runnable {
        private final RedisConnection redisConnection;
        private final byte[][] keyBytes;

        private AsyncMessagePassiveProcessing(RedisConnection redisConnection, byte[][] keyBytes) {
            Assert.notNull(redisConnection, "redisConnection is not null");
            this.keyBytes = keyBytes;
            this.redisConnection = redisConnection;
        }

        @Override
        public void run() {
            if (keyBytes.length > 0) {
                //订阅模式监听rpc数据包，存在多个任务重复消费问题
                redisConnection.subscribe((message, pattern) -> {
                    try {
                        if(redisInvokerProperties.isEnableSubscribeLock()){
                            RedisRequest redisRequest = redisMessageSerialize.decode(message.getBody(), RedisRequest.class);
                            byte[] nxKeyBytes = redisRequest.getRequestId().getBytes();
                            //分布式锁，让多个消息执行1次
                            boolean pExpire = redisConnection.eval((
                                    "if redis.call('setnx', KEYS[1], 1) == 1 then " +
                                    "   redis.call('pexpire', KEYS[1], ARGV[1]);" +
                                    "   return 1;" +
                                    "end;" +
                                    "   return 0;").getBytes(), ReturnType.BOOLEAN, 1, nxKeyBytes, String.format("%s", redisRequest.getReadTimeout()).getBytes());
                            if (pExpire) {
                                try {
                                    mainLoop(message);
                                } finally {
                                    redisConnection.del(nxKeyBytes);
                                }
                            }
                        }else{
                            mainLoop(message);
                        }
                    } catch (Exception ex) {
                        logger.error("redis rpc Subscription message processing failed {}", ex);
                        throw new RedisMessageException(ex);
                    }
                }, keyBytes);
            }
        }

        /**
         * 主程序执行队列消息
         *
         * @throws InterruptedException
         * @throws IOException
         */
        private void mainLoop(Message message) throws Exception {
            RedisRequest redisRequest = redisMessageSerialize.decode(message.getBody(), RedisRequest.class);
            try {
                String channelKey = new String(message.getChannel());
                //执行消息监听
                Optional.ofNullable(messageListenerMap.get(channelKey)).ifPresent(redisMessageListeners -> {
                    try {
                        for (RedisSubscribeMessageListener redisMessageListener : redisMessageListeners) {
                            //处理订阅消息
                            RedisResponse redisResponse = redisMessageListener.handleDelivery(channelKey, redisRequest);
                            redisConnection.publish(redisResponse.getReplyTo().getBytes(), redisMessageSerialize.encode(redisResponse));
                        }
                    } catch (Exception ex) {
                        throw new RedisMessageException(ex);
                    }
                });
            } catch (Exception ex) {
                logger.warn("redis rpc message error {}", ex);

                RedisResponse redisResponse = RedisResponse.builder()
                        .replyTo(redisRequest.getReplyTo())
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .responseId(redisRequest.getRequestId())
                        .body(ex.getMessage().getBytes())
                        .build();
                redisConnection.publish(redisResponse.getReplyTo().getBytes(), redisMessageSerialize.encode(redisResponse));
            }
        }
    }


    /**
     * 异步队列执行-主动模式
     */
    private class AsyncMessageActiveProcessing implements Runnable {
        private final RedisConnection redisConnection;
        private final byte[][] keyBytes;

        private AsyncMessageActiveProcessing(RedisConnection redisConnection, byte[][] keyBytes) {
            Assert.notNull(redisConnection, "redisConnection is not null");

            this.keyBytes = keyBytes;
            this.redisConnection = redisConnection;
        }

        @Override
        public void run() {

            //主动模式 - 获取队列中的消rpc数据包，不存在多任务消费问题
            Consumer<byte[]> executeQueueConsumer = key -> Optional.ofNullable(redisConnection.lPop(key)).ifPresent(body -> {
                getTaskExecutor().execute(() -> {
                    try {
                        RedisRequest redisRequest = redisMessageSerialize.decode(body, RedisRequest.class);
                        mainLoop(key, redisRequest);
                    } catch (Exception ex) {
                        logger.error("redis rpc Subscription message processing failed {}", ex);
                    }
                });
            });

            while (isRunning()) {
                try {
                    if (checkConnection()) {
                        for (byte[] key : keyBytes) {
                            executeQueueConsumer.accept(key);
                        }
                    }
                    Thread.sleep(100);
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        logger.warn("redis rpc Container has stopped listening");
                        return;
                    }
                    logger.warn("redis rpc container listening fail {}", ex.getMessage());
                }
            }
        }

        /**
         * 检查链接是否可用
         *
         * @return
         */
        private boolean checkConnection() {
            if (!this.redisConnection.isClosed()) {
                try {
                    String pong = this.redisConnection.ping();
                    if (!"PONG".equals(pong)) {
                        for (RedisMessageListener redisMessageListener : messageListenerList) {
                            redisMessageListener.handleShutdownSignal(redisConnectionFactory, new RedisPingException(pong));
                        }
                    }
                    return true;
                } catch (Exception ex) {

                    logger.warn("redis connection check error {}", ex.getMessage());

                    for (RedisMessageListener redisMessageListener : messageListenerList) {
                        redisMessageListener.handleShutdownSignal(redisConnectionFactory, ex);
                    }
                }
            } else {
                for (RedisMessageListener redisMessageListener : messageListenerList) {
                    redisMessageListener.handleCancel(redisConnectionFactory, new RedisCloseException("close"));
                }
            }
            return false;
        }

        /**
         * 主程序执行队列消息
         *
         * @throws InterruptedException
         * @throws IOException
         */
        private void mainLoop(byte[] channel, RedisRequest redisRequest) throws Exception {
            try {
                String channelKey = new String(channel);
                //执行消息监听
                Optional.ofNullable(messageListenerMap.get(channelKey)).ifPresent(redisMessageListeners -> {
                    try {
                        for (RedisSubscribeMessageListener redisMessageListener : redisMessageListeners) {
                            //处理订阅消息
                            RedisResponse redisResponse = redisMessageListener.handleDelivery(channelKey, redisRequest);
                            redisConnection.eval(
                                    (
                                     "redis.call('LPUSH', KEYS[1], ARGV[1]); " +
                                     "redis.call('pexpire', KEYS[1], ARGV[2]);"
                                    ).getBytes(),
                                    ReturnType.BOOLEAN,
                                    1,
                                    redisResponse.getReplyTo().getBytes(),
                                    redisMessageSerialize.encode(redisResponse),
                                    String.format("%s", redisRequest.getReadTimeout()).getBytes()
                            );

                        }
                    } catch (Exception ex) {
                        throw new RedisMessageException(ex);
                    }
                });
            } catch (Exception ex) {
                logger.warn("redis rpc message error {}", ex);
                RedisResponse redisResponse = RedisResponse.builder()
                        .replyTo(redisRequest.getReplyTo())
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .responseId(redisRequest.getRequestId())
                        .body(ex.getMessage().getBytes())
                        .build();
                redisConnection.eval(
                        (
                            "redis.call('LPUSH', KEYS[1], ARGV[1]); " +
                            "redis.call('pexpire', KEYS[1], ARGV[2]);"
                        ).getBytes(),
                        ReturnType.BOOLEAN,
                        1,
                        redisResponse.getReplyTo().getBytes(),
                        redisMessageSerialize.encode(redisResponse),
                        String.format("%s", redisRequest.getReadTimeout()).getBytes()
                );
            }
        }
    }
}
