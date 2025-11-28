package com.i360day.invoker.configuration;

import com.i360day.invoker.AmqpInvokerRequest;
import com.i360day.invoker.AmqpMessageListenerContainer;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.executor.AmqpInvokerRequestExecutor;
import com.i360day.invoker.interceptor.InvokerRequestInterceptor;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.thread.InvokerThreadPoolTaskExecutor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liju.z
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@AutoConfigureBefore(InvokerAutoConfiguration.class)
public class AmqpInvokerAutoConfiguration {

    /**
     * amqp message listener container
     * @param invokerProperties
     * @param connectionFactory
     * @return
     */
    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public AmqpMessageListenerContainer amqpMessageListenerContainer(InvokerProperties invokerProperties,
                                                                     ConnectionFactory connectionFactory,
                                                                     InvokerThreadPoolTaskExecutor invokerThreadPoolTaskExecutor) {
        AmqpMessageListenerContainer amqpMessageListenerContainer = new AmqpMessageListenerContainer(connectionFactory, 1000, 1000, invokerProperties);
        amqpMessageListenerContainer.setTaskExecutor(invokerThreadPoolTaskExecutor);
        return amqpMessageListenerContainer;
    }

    /**
     * create Amqp Request
     * @param connectionFactory
     * @param invokerProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpInvokerRequest amqpHttpInvokerRequest(ConnectionFactory connectionFactory, InvokerProperties invokerProperties){
        return new AmqpInvokerRequest(connectionFactory, invokerProperties);
    }

    /**
     * amqp 请求执行器
     * @param invokerRequest
     * @param invokerContext
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpInvokerRequestExecutor amqpHttpInvokerRequestExecutor(AmqpInvokerRequest invokerRequest, InvokerContext invokerContext){
        return new AmqpInvokerRequestExecutor(invokerRequest, invokerContext.getBeanOfMap(InvokerRequestInterceptor.class).values());
    }
}
