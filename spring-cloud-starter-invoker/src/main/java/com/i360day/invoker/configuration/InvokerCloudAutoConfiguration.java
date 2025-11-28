package com.i360day.invoker.configuration;

import com.i360day.invoker.CloudInvokerRequest;
import com.i360day.invoker.context.CloudInvokerContext;
import com.i360day.invoker.executor.CloudInvokerRequestExecutor;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.interceptor.InvokerRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HttpInvoker Cloud Auto Configuration
 *
 * @Author liju.z
 * @Date 2025/3/10 21:00
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(InvokerAutoConfiguration.class)
public class InvokerCloudAutoConfiguration {

    /**
     * 自定义spring上下文
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CloudInvokerContext httpInvokerContext() {
        return new CloudInvokerContext();
    }

    /**
     * create cloud request
     *
     * @param delegate
     * @param loadBalancerClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CloudInvokerRequest cloudHttpInvokerRequest(InvokerClient delegate, LoadBalancerClient loadBalancerClient) {
        return new CloudInvokerRequest(delegate, loadBalancerClient);
    }

    /**
     * create cloud request executor
     *
     * @param invokerRequest
     * @param invokerContext
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CloudInvokerRequestExecutor cloudHttpInvokerRequestExecutor(CloudInvokerRequest invokerRequest, CloudInvokerContext invokerContext) {
        return new CloudInvokerRequestExecutor(invokerRequest, invokerContext.getBeanOfMap(InvokerRequestInterceptor.class).values());
    }

    @Bean
    @ConditionalOnMissingBean
    public InvokerRequestInterceptor simpleRequestInterceptor() {
        return requestTemplate -> {

        };
    }
}
