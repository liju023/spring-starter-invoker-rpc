package com.i360day.invoker;

public interface RedisSubscribeMessageListener extends RedisMessageListener{
    /**
     * 队列接收到消息处理
     * @param keys
     * @param redisRequest
     */
    RedisResponse handleDelivery(String keys, RedisRequest redisRequest) ;
}
