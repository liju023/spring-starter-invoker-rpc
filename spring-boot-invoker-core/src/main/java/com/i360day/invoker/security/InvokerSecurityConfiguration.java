package com.i360day.invoker.security;

import com.i360day.invoker.annotation.RemoteIgnoreSecurity;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.support.RemoteExporter;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @program: spring-cloud-invoker-parent
 * @description: HTTPInvoker config
 * @author: liju.z
 * @create: 2021-03-16 09:53
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = {"org.springframework.security.web.SecurityFilterChain"})
//@ConditionalOnBean(type = {"org.springframework.security.config.annotation.web.configuration.HttpSecurityConfiguration.httpSecurity"})
public class InvokerSecurityConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public InvokerSecurityCustomizer httpInvokerSecurityIgnoreService() {
        return new InvokerSecurityIgnoreService();
    }

    /**
     * security filter
     *
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Order(Integer.MIN_VALUE)
    @ConditionalOnMissingBean
    @Bean(name = InvokerConstant.INVOKER_SECURITY_FILTER_CHAIN)
    public SecurityFilterChain invokerSecurityFilterChain(HttpSecurity httpSecurity, InvokerSecurityCustomizer customizer) throws Exception {
        Set<String> ignoreUrlSet = new HashSet<>();

        //AbstractHandlerMapping
        Map<String, RemoteExporter> beansOfType = applicationContext.getBeansOfType(RemoteExporter.class);
        beansOfType.forEach((url, remoteExporter) -> {
            RemoteIgnoreSecurity remoteIgnoreSecurity = AnnotationUtils.findAnnotation(AopProxyUtils.ultimateTargetClass(remoteExporter.getService()), RemoteIgnoreSecurity.class);
            if (remoteIgnoreSecurity != null) {
                ignoreUrlSet.add(url);
            }
        });

        //设置ignore service
        HttpSecurity CustomizeHttpSecurity;
        if (ignoreUrlSet.isEmpty()) {
            CustomizeHttpSecurity = httpSecurity
                    .securityMatcher(InvokerConstant.FIXED_URL)
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(r -> {
                        r.anyRequest().authenticated();
                    });
        } else {
            //不验证地址,放行....
            CustomizeHttpSecurity = httpSecurity
                    .securityMatcher(InvokerConstant.FIXED_URL)
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(r -> {
                        r.requestMatchers(ignoreUrlSet.stream().toArray(String[]::new)).permitAll().anyRequest().authenticated();
                    });
        }

        //自定义
        customizer.customize(CustomizeHttpSecurity);
        return CustomizeHttpSecurity.build();
    }
}
