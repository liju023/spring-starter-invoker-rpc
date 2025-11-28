package com.i360day.invoker.proxy;

import com.i360day.invoker.hystrix.SetterFactory;
import com.i360day.invoker.properties.InvokerHystrixProperties;
import com.i360day.invoker.properties.InvokerProperties;
import com.netflix.hystrix.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liju.z
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({InvokerHystrixProperties.class, InvokerProperties.class})
@ConditionalOnProperty(value = "spring.invoker.hystrix.enabled", matchIfMissing = true)
public class HystrixProxyConfiguration {

    /**
     * create Targeter
     *
     * @param setterFactory
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public Targeter hystrixTargeter(SetterFactory setterFactory) {
        return new HystrixTargeter(setterFactory);
    }

    /**
     * create SetterFactory
     *
     * @param invokerHystrixProperties
     * @param invokerProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SetterFactory setterFactory(InvokerHystrixProperties invokerHystrixProperties, InvokerProperties invokerProperties) {
        HystrixCommandProperties.Setter setGet = invokerHystrixProperties.getSetGet();
        Integer timeout = Optional.ofNullable(setGet.getExecutionTimeoutInMilliseconds()).orElse(invokerProperties.getRequest().getReadTimeout());
        setGet.withExecutionTimeoutInMilliseconds(timeout);

        HystrixThreadPoolProperties.Setter threadSetter = invokerHystrixProperties.getThreadSetter();

        return (target, method) -> {

            String commandKey = String.format("%s#%s(%s)", method.getDeclaringClass().getName(), method.getName(), Stream.of(method.getParameterTypes()).map(m -> m.toString()).collect(Collectors.joining(",")));

            return HystrixCommand.Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(target.getName()))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(commandKey))
                    .andCommandPropertiesDefaults(setGet)
                    .andThreadPoolPropertiesDefaults(threadSetter)
                    .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey));
        };
    }
}
