package com.i360day.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.codes.decoder.JavaObjectDecoder;
import com.i360day.invoker.codes.decoder.SpringDecode;
import com.i360day.invoker.codes.encoder.Encoder;
import com.i360day.invoker.codes.encoder.JavaObjectEncoder;
import com.i360day.invoker.codes.encoder.SpringEncoder;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.converter.HttpInvokerHttpMessageConverter;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.http.httpclient.InvokerHttpClient;
import com.i360day.invoker.http.okhttp.InvokerOkHttpClient;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.proxy.DefaultTargeterHandler;
import com.i360day.invoker.proxy.Targeter;
import com.i360day.invoker.security.InvokerSecurityAdapter;
import com.i360day.invoker.support.*;
import com.i360day.invoker.thread.InvokerThreadPoolTaskExecutor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liju.z
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({InvokerProperties.class})
@AutoConfigureAfter(InvokerBasicConfiguration.HttpInvokerRequestClient.class)
public class InvokerBasicConfiguration {

    /**
     * application/x-rpc-http-invoker转换器
     *
     * @see InvokerConstant#ACCEPT_RPC_HTTP_INVOKER
     * @param objectMapper
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpInvokerHttpMessageConverter httpInvokerHttpMessageConverter(ObjectMapper objectMapper){
        return new HttpInvokerHttpMessageConverter(objectMapper);
    }

    /**
     * 服务跟踪拦截器
     * @return
     */
    @Bean(name = InvokerConstant.REMOTE_INVOCATION_TRACE_INTERCEPTOR)
    @ConditionalOnMissingBean(name = InvokerConstant.REMOTE_INVOCATION_TRACE_INTERCEPTOR, value = RemoteInvocationTraceInterceptor.class)
    public RemoteInvocationTraceInterceptor remoteInvocationTraceInterceptor(){
        return new DefaultRemoteInvocationTraceInterceptor();
    }

    /**
     * 服务调用执行器
     * @return
     */
    @Bean(name = InvokerConstant.REMOTE_INVOCATION_EXECUTOR)
    @ConditionalOnMissingBean(name = InvokerConstant.REMOTE_INVOCATION_EXECUTOR, value = RemoteInvocationExecutor.class)
    public RemoteInvocationExecutor remoteInvocationExecutor(InvokerSecurityAdapter invokerSecurityAdapter){
        return new DefaultRemoteInvocationExecutor(invokerSecurityAdapter);
    }

    /**
     * 创建httpClient request
     *
     * @return
     */
    @ConditionalOnMissingBean
    @Bean(destroyMethod = "close")
    public InvokerClient defaultHttpClient(InvokerProperties properties) {
        return new InvokerClient.Default(properties);
    }

    /**
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public InvokerContext httpInvokerContext() {
        return new DefaultInvokerContext();
    }

    /**
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public InvokerSecurityAdapter httpInvokerSecurityAdapter() {
        return new NullInvokerSecurityAdapter();
    }

    /**
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.invoker.hystrix.enabled", havingValue = "false", matchIfMissing = true)
    public Targeter defaultTargeter() {
        return new DefaultTargeterHandler();
    }

    /**
     * 创建线程池
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public InvokerThreadPoolTaskExecutor invokerThreadPoolTaskScheduler(){
        InvokerThreadPoolTaskExecutor executor = new InvokerThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("invoker-thread-pool");
        executor.setThreadGroupName("invoker-thread-group");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        return executor;
    }

    /**
     * http
     *
     * @author liju.z
     */
    @Configuration(proxyBeanMethods = false)
    public static class HttpInvokerRequestClient {
        /**
         * 创建httpClient request
         *
         * @return
         */
        @ConditionalOnMissingBean
        @Bean(destroyMethod = "close")
        @ConditionalOnProperty(value = "spring.invoker.request.client", havingValue = "httpClient", matchIfMissing = true)
        public InvokerClient httpClient(InvokerProperties properties) {
            return InvokerHttpClient.create(properties);
        }

        /**
         * 创建okHttp request
         *
         * @return
         */
        @ConditionalOnMissingBean
        @Bean(destroyMethod = "close")
        @ConditionalOnProperty(value = "spring.invoker.request.client", havingValue = "okhttp")
        public InvokerClient okHttpClient(InvokerProperties properties) {
            return InvokerOkHttpClient.create(properties);
        }
    }

    /**
     * json Serializable
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.invoker.serializable", havingValue = "json", matchIfMissing = true)
    public static class JsonSerializableConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Decoder springDecode(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringDecode(messageConverters);
        }

        @Bean
        @ConditionalOnMissingBean
        public RemoteInvocationFactory objectMapperRemoteInvocationFactory(ObjectMapper objectMapper) {
            return new ObjectMapperRemoteInvocationFactory(objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean
        public Encoder springEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringEncoder(messageConverters);
        }
    }

    /**
     * jdk Serializable
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.invoker.serializable", havingValue = "jdk")
    public static class JavaSerializableConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Decoder javaObjectDecoder() {
            return new JavaObjectDecoder();
        }

        @Bean
        @ConditionalOnMissingBean
        public Encoder javaObjectEncoder() {
            return new JavaObjectEncoder();
        }

        @Bean
        @ConditionalOnMissingBean
        public RemoteInvocationFactory remoteInvocationFactory() {
            return new JavaRemoteInvocationFactory();
        }
    }
}
