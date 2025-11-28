package com.i360day.invoker;

import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * redis消息监听
 */
public interface RedisMessageListener {
    /**
     * 队列关闭监听
     * @param sig
     */
    default void handleShutdownSignal(RedisConnectionFactory connectionFactory, Exception sig){

    }

    /**
     * 队列取消执行
     * @param exception
     */
    default void handleCancel(RedisConnectionFactory connectionFactory, Exception exception){

    }
}
