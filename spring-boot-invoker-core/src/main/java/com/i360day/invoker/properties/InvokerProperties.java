package com.i360day.invoker.properties;

import com.i360day.invoker.hystrix.FallbackFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author liju.z
 */
@ConfigurationProperties(prefix = "spring.invoker")
public class InvokerProperties {
    /**
     * 是安全的ssl证书
     */
    private boolean isSecure = false;
    /**
     * 默认序列化 json。
     * jdk序列化 SerializableType.jdk
     * json序列化 SerializableType.json
     */
    private SerializableType serializable;
    /**
     * 请求客户端
     */
    private HttpInvokerRequestProperties request = new HttpInvokerRequestProperties();
    /**
     * 客户端全局配置
     */
    private HttpInvokerClientProperties globalClient;
    /**
     * 服务端全局配置
     */
    private HttpInvokerServerProperties globalServer;

    public InvokerProperties() {
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }
    public HttpInvokerRequestProperties getRequest() {
        return request;
    }

    public void setRequest(HttpInvokerRequestProperties request) {
        this.request = request;
    }

    public HttpInvokerClientProperties getGlobalClient() {
        return globalClient;
    }

    public void setGlobalClient(HttpInvokerClientProperties globalClient) {
        this.globalClient = globalClient;
    }

    public SerializableType getSerializable() {
        return serializable;
    }

    public void setSerializable(SerializableType serializable) {
        this.serializable = serializable;
    }

    public HttpInvokerServerProperties getGlobalServer() {
        return globalServer;
    }

    public void setGlobalServer(HttpInvokerServerProperties globalServer) {
        this.globalServer = globalServer;
    }

    /**
     * 请求
     */
    public static class HttpInvokerRequestProperties {
        /**
         * 请求客户端
         * 1、httpClient
         * 2、okhttp
         */
        private ClientType client = ClientType.httpClient;
        /**
         * 连接请求超时时间
         */
        private int connectionRequestTimeout = (int) TimeUnit.SECONDS.toMillis(15);
        /**
         * 连接超时时间
         */
        private int connectTimeout = (int) TimeUnit.SECONDS.toMillis(5);
        /**
         * 读取超时时间
         */
        private int readTimeout = (int) TimeUnit.SECONDS.toMillis(30);
        /**
         * 最大链接数
         */
        private int maxConnections = 100;
        /**
         * 最大空闲链接，单位毫秒
         */
        private int timeToLive = (int) TimeUnit.SECONDS.toMillis(30);

        public ClientType getClient() {
            return client;
        }

        public void setClient(ClientType client) {
            this.client = client;
        }

        public int getConnectionRequestTimeout() {
            return connectionRequestTimeout;
        }

        public void setConnectionRequestTimeout(int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        public int getTimeToLive() {
            return timeToLive;
        }

        public void setTimeToLive(int timeToLive) {
            this.timeToLive = timeToLive;
        }
    }


    /**
     * 客户端
     */
    public static class HttpInvokerClientProperties {
        /**
         * 客户端账户
         */
        private String clientUser;
        /**
         * 客户端密码
         */
        private String clientPassword;
        /**
         * 回调降级
         */
        private Class<?> fallback;
        /**
         * 回调工厂
         */
        private Class<? extends FallbackFactory<Object>> fallbackFactory;

        public String getClientUser() {
            return clientUser;
        }

        public void setClientUser(String clientUser) {
            this.clientUser = clientUser;
        }

        public String getClientPassword() {
            return clientPassword;
        }

        public void setClientPassword(String clientPassword) {
            this.clientPassword = clientPassword;
        }

        public Class<? extends FallbackFactory<Object>> getFallbackFactory() {
            return fallbackFactory;
        }

        public void setFallbackFactory(Class<? extends FallbackFactory<Object>> fallbackFactory) {
            this.fallbackFactory = fallbackFactory;
        }

        public Class<?> getFallback() {
            return fallback;
        }

        public void setFallback(Class<?> fallback) {
            this.fallback = fallback;
        }
    }

    /**
     * 服务端
     */
    public static class HttpInvokerServerProperties{
        /**
         * 服务端-账户
         */
        private String serverUser;
        /**
         * 服务端-密码
         */
        private String serverPassword;

        public String getServerUser() {
            return serverUser;
        }

        public void setServerUser(String serverUser) {
            this.serverUser = serverUser;
        }

        public String getServerPassword() {
            return serverPassword;
        }

        public void setServerPassword(String serverPassword) {
            this.serverPassword = serverPassword;
        }
    }


    /**
     * 序列化
     */
    public enum SerializableType{
        //jdk序列化
        jdk,
        //ObjectMapper序列化
        json,
        ;
    }

    /**
     * 客户端类型
     */
    public enum ClientType{
        httpClient, okhttp
    }
}
