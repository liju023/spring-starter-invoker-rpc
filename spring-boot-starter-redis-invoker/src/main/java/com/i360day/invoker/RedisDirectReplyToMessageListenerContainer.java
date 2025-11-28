package com.i360day.invoker;

import com.i360day.invoker.codes.RedisMessageSerialize;
import com.i360day.invoker.exception.RedisCloseException;
import com.i360day.invoker.exception.RedisPingException;
import com.i360day.invoker.properties.ModeType;
import com.i360day.invoker.support.AbstractMessageContainerSmartLifecycle;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 * redis直接回复消息监听器
 *
 * @Author liju.z
 * @Date 2025/1/25 15:28
 */
public class RedisDirectReplyToMessageListenerContainer extends AbstractMessageContainerSmartLifecycle {

    private RedisConnection redisConnection;
    private String redisQueueReplyTo = "redis.queue.reply-to";
    private final RedisConnectionFactory redisConnectionFactory;
    private final ModeType modeType;
    private final RedisMessageSerialize redisMessageSerialize;
    private final List<RedisReplyToMessageListener> listenerList = new Vector<>();

    public RedisDirectReplyToMessageListenerContainer(String redisQueueReplyTo, RedisConnectionFactory redisConnectionFactory, RedisMessageSerialize redisMessageSerialize, ModeType modeType) {
        this(redisConnectionFactory, redisMessageSerialize, modeType);
        this.redisQueueReplyTo = redisQueueReplyTo;
    }

    public RedisDirectReplyToMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, RedisMessageSerialize redisMessageSerialize, ModeType modeType) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisMessageSerialize = redisMessageSerialize;
        this.modeType = modeType;
    }


    public String getRedisQueueReplyTo() {
        return redisQueueReplyTo;
    }

    /**
     * 添加消息处理器
     *
     * @param messageListener
     */
    public void addListener(RedisReplyToMessageListener messageListener) {
        this.listenerList.add(messageListener);
    }

    /**
     * 获取链接
     */
    public RedisConnection getRedisConnection() {
        return redisConnection;
    }

    /**
     * 启动
     */
    @Override
    protected void doStart() {
        this.redisConnection = this.redisConnectionFactory.getConnection();
        //主动模式，lpush队列消息
        if (ModeType.ACTIVE.equals(modeType)) {
            getTaskExecutor().execute(new AsyncMessageActiveProcessing(this.redisConnection));
        }
        //被动模式，订阅消息
        else {
            getTaskExecutor().execute(new AsyncMessagePassiveProcessing(this.redisConnection));
        }
    }

    /**
     * 前置执行检查链接
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.redisConnectionFactory, "redisConnection is required");
    }

    /**
     * 发布订阅消息
     *
     * @param channel
     * @param message
     * @param run
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T subscribe(byte[] channel, byte[] message, ExecuteCallback<T> run) throws Exception {
        RedisConnection redisConnection = getRedisConnection();
        if (redisConnection == null || redisConnection.isClosed()) {
            throw new IllegalCallerException("connection is null or close");
        }
        //发布订阅消息
        redisConnection.publish(channel, message);

        return run.call(redisConnection);
    }

    /**
     * 发布队列队列消息
     *
     * @param channel
     * @param message
     * @param run
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T sendQueueMessage(byte[] channel, byte[] message, int expireTime, ExecuteCallback<T> run) throws Exception {
        RedisConnection redisConnection = getRedisConnection();
        if (redisConnection == null || redisConnection.isClosed()) {
            throw new IllegalCallerException("connection is null or close");
        }
        //发布队列消息
        redisConnection.eval(
                (
                    "redis.call('LPUSH', KEYS[1], ARGV[1]); " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]);"
                ).getBytes(),
                ReturnType.BOOLEAN,
                1,
                channel,
                message,
                String.format("%s", expireTime).getBytes()
        );

        return run.call(redisConnection);
    }

    public interface ExecuteCallback<T> {
        T call(RedisConnection redisConnection) throws Exception;
    }

    /**
     * 异步队列执行-主动模式
     */
    private class AsyncMessagePassiveProcessing implements Runnable {
        private final RedisConnection redisConnection;

        private AsyncMessagePassiveProcessing(RedisConnection redisConnection) {
            Assert.notNull(redisConnection, "redisConnection is not null");
            this.redisConnection = redisConnection;
        }

        @Override
        public void run() {
            //监听rpc订阅消息，每个连接key都是唯一。不存在多任务消费
            this.redisConnection.subscribe((message, pattern) -> {
                String keys = new String(message.getChannel());
                try {
                    RedisResponse redisResponse = redisMessageSerialize.decode(message.getBody(), RedisResponse.class);
                    for (RedisReplyToMessageListener redisMessageListener : listenerList) {
                        redisMessageListener.handleDelivery(keys, redisResponse);
                    }
                } catch (Exception ex) {
                    logger.warn("redis rpc Subscription message processing failed {}", ex);
                }
            }, redisQueueReplyTo.getBytes());
        }
    }

    /**
     * 异步队列执行-主动模式
     */
    private class AsyncMessageActiveProcessing implements Runnable {
        private final RedisConnection redisConnection;

        private AsyncMessageActiveProcessing(RedisConnection redisConnection) {
            Assert.notNull(redisConnection, "redisConnection is not null");
            this.redisConnection = redisConnection;
        }

        @Override
        public void run() {
            while (isRunning()) {
                try {
                    if (checkConnection()) {
                        Optional.ofNullable(redisConnection.lPop(redisQueueReplyTo.getBytes())).ifPresent(body -> {
                            getTaskExecutor().execute(() -> {
                                RedisResponse redisResponse = null;
                                try {
                                    redisResponse = redisMessageSerialize.decode(body, RedisResponse.class);
                                    for (RedisReplyToMessageListener redisMessageListener : listenerList) {
                                        redisMessageListener.handleDelivery(redisQueueReplyTo, redisResponse);
                                    }
                                } catch (Exception ex) {
                                    logger.warn("redis rpc message error {}", ex);

                                    for (RedisReplyToMessageListener redisMessageListener : listenerList) {
                                        redisMessageListener.handleDelivery(redisQueueReplyTo, RedisResponse.builder()
                                                .replyTo(redisQueueReplyTo)
                                                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .responseId(redisResponse != null ? redisResponse.getRequestId() : "not-id")
                                                .body(ex.getMessage().getBytes())
                                                .build());
                                    }
                                }
                            });
                        });
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
                        for (RedisMessageListener redisMessageListener : listenerList) {
                            redisMessageListener.handleShutdownSignal(redisConnectionFactory, new RedisPingException(pong));
                        }
                    }
                    return true;
                } catch (Exception ex) {

                    logger.warn("redis connection check error {}", ex.getMessage());

                    for (RedisMessageListener redisMessageListener : listenerList) {
                        redisMessageListener.handleShutdownSignal(redisConnectionFactory, ex);
                    }
                }
            } else {
                for (RedisMessageListener redisMessageListener : listenerList) {
                    redisMessageListener.handleCancel(redisConnectionFactory, new RedisCloseException("close"));
                }
            }
            return false;
        }
    }
}
