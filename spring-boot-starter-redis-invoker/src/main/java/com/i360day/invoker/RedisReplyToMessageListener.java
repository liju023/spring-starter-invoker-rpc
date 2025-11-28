package com.i360day.invoker;

public interface RedisReplyToMessageListener extends RedisMessageListener{
    /**
     * 队列接收到消息处理
     * @param keys
     * @param redisResponse
     */
    void handleDelivery(String keys, RedisResponse redisResponse) ;
}
