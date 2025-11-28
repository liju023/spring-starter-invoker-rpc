package com.i360day.invoker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * socket 配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.invoker.web-socket")
public class InvokerWebSocketProperties {
    private String endpoint = "httpInvoker";
    /**
     * 设置最大空闲超时
     */
    private int maxIdleTimeout = (int) TimeUnit.SECONDS.toMillis(30);
    /**
     * 设置此会话可以缓冲的传入二进制消息的最大长度。
     */
    private int maxBinaryMessageBufferSize = 1024 * 8;
    /**
     * 设置此会话可以缓冲的传入文本消息的最大长度。
     */
    private int maxTextMessageBufferSize = 1024 * 8;
    /**
     * 心跳时间
     */
    private long heartbeat = TimeUnit.SECONDS.toMillis(5);
    /**
     * 服务端用户
     */
    private String serverUserName;
    /**
     * 服务端密码
     */
    private String serverPassword;

    public String getEndpoint() {
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    public void setMaxIdleTimeout(int maxIdleTimeout) {
        this.maxIdleTimeout = maxIdleTimeout;
    }

    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    public void setMaxBinaryMessageBufferSize(int maxBinaryMessageBufferSize) {
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    public void setMaxTextMessageBufferSize(int maxTextMessageBufferSize) {
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(long heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getServerUserName() {
        return serverUserName;
    }

    public void setServerUserName(String serverUserName) {
        this.serverUserName = serverUserName;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }
}