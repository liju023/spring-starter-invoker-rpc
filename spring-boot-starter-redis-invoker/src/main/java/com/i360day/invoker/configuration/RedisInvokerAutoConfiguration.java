package com.i360day.invoker.configuration;

import com.i360day.invoker.RedisInvokerRequest;
import com.i360day.invoker.RedisMessageListenerContainer;
import com.i360day.invoker.codes.RedisMessageSerialize;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.executor.RedisInvokerRequestExecutor;
import com.i360day.invoker.interceptor.InvokerRequestInterceptor;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.properties.RedisInvokerProperties;
import com.i360day.invoker.thread.InvokerThreadPoolTaskExecutor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author liju.z
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@AutoConfigureBefore(InvokerAutoConfiguration.class)
@EnableConfigurationProperties(RedisInvokerProperties.class)
public class RedisInvokerAutoConfiguration {

    /**
     * redis rpc server消息容器
     *
     * @param redisConnectionFactory
     * @param redisInvokerProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       RedisInvokerProperties redisInvokerProperties,
                                                                       RedisMessageSerialize redisMessageSerialize,
                                                                       InvokerThreadPoolTaskExecutor invokerThreadPoolTaskExecutor) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer(redisConnectionFactory, redisInvokerProperties, redisMessageSerialize, 1000);
        redisMessageListenerContainer.setTaskExecutor(invokerThreadPoolTaskExecutor);
        return redisMessageListenerContainer;
    }

    /**
     * create Redis Request
     *
     * @param redisConnectionFactory
     * @param invokerProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisInvokerRequest redisHttpInvokerRequest(RedisConnectionFactory redisConnectionFactory,
                                                       InvokerProperties invokerProperties,
                                                       RedisMessageSerialize redisMessageSerialize,
                                                       RedisInvokerProperties redisInvokerProperties) {
        return new RedisInvokerRequest(redisConnectionFactory, invokerProperties, redisMessageSerialize, redisInvokerProperties);
    }

    /**
     * redis request executor
     *
     * @param invokerRequest
     * @param invokerContext)
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisInvokerRequestExecutor redisHttpInvokerRequestExecutor(RedisInvokerRequest invokerRequest, InvokerContext invokerContext) {
        return new RedisInvokerRequestExecutor(invokerRequest, invokerContext.getBeanOfMap(InvokerRequestInterceptor.class).values());
    }

    /**
     * redis 消息数据包序列化
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisMessageSerialize redisMessageSerialize(){
        return new RedisMessageSerialize();
    }
}
