package com.i360day.invoker.configuration;

import com.i360day.invoker.DefaultHttpInvokerRequest;
import com.i360day.invoker.InvokerBasicConfiguration;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.executor.HttpComponentsInvokerRequestExecutor;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.interceptor.InvokerRequestInterceptor;
import com.i360day.invoker.security.InvokerSecurityConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liju.z
 */
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(value = {InvokerSecurityConfiguration.class, InvokerBasicConfiguration.class})
public class InvokerAutoConfiguration {

    /**
     * default HttpInvokerRequest
     * @param invokerClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultHttpInvokerRequest defaultHttpInvokerClient(InvokerClient invokerClient) {
        return new DefaultHttpInvokerRequest(invokerClient);
    }

    /**
     * 创建请求执行器
     * @param invokerContext
     * @param invokerRequest
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpComponentsInvokerRequestExecutor httpInvokerRequestExecutor(InvokerContext invokerContext, DefaultHttpInvokerRequest invokerRequest) {
        return new HttpComponentsInvokerRequestExecutor(invokerRequest, invokerContext.getBeanOfMap(InvokerRequestInterceptor.class).values());
    }
}
