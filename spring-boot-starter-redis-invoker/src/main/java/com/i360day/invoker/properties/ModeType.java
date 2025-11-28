package com.i360day.invoker.properties;

/**
 * redis 消息模式
 */
public enum ModeType {
    /**
     * 被动模式：订阅消息
     */
    PASSIVE,
    /**
     * 主动模式：lpush队列
     */
    ACTIVE,
    /**
     * 未知模式
     * RemoteRedisClient未指定则使用配置中的模式，默认使用被动模式
     */
    UNKNOWN;
}