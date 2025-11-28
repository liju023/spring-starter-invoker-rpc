package com.i360day.invoker;

import com.rabbitmq.client.Address;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.util.List;

public final class AmqpAddress {
    /**
     * amqp 回复队列
     */
    public static final String REPLY_QUEUE_KEY = "amq.rabbitmq.reply-to";
    /**
     * amqp invoker 声明队交换机
     */
    public final static String EXCHANGE_KEY = "amqp.invoker.rpc.exchange";
    /**
     * amqp invoker 声明队列
     */
    public static final String AMQP_INVOKER_RPC_QUEUE = "amqp.invoker.rpc.%s.queue";

    /**
     * 创建CachingConnectionFactory工厂
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param virtualHost
     * @return
     */
    public static CachingConnectionFactory createConnectionFactory(String ip, int port, String username, String password, String virtualHost){
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setHost(ip);
        cachingConnectionFactory.setPort(port);
        cachingConnectionFactory.setVirtualHost(virtualHost);
        cachingConnectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CONNECTION);
        return cachingConnectionFactory;
    }

    /**
     * 创建集群方式 CachingConnectionFactory工厂
     * @param addressList
     * @param username
     * @param password
     * @param virtualHost
     * @return
     */
    public static CachingConnectionFactory createConnectionFactory(List<Address> addressList, String username, String password, String virtualHost){
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setAddressResolver(() -> addressList);
        cachingConnectionFactory.setVirtualHost(virtualHost);
        cachingConnectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CONNECTION);
        return cachingConnectionFactory;
    }
}
