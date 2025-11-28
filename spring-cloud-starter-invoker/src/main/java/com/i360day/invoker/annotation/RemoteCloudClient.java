package com.i360day.invoker.annotation;

import com.i360day.invoker.CloudInvokerClientFactoryBean;
import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.codes.encoder.Encoder;
import com.i360day.invoker.hystrix.DefaultFallbackFactory;
import com.i360day.invoker.hystrix.FallbackFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * remote cloud client
 */
@Inherited
@Documented
@RemoteClient
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface RemoteCloudClient {
    /**
     * 客户端代理类
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    Class<? extends FactoryBean> clientProxyClass() default CloudInvokerClientFactoryBean.class;

    /**
     * 解码器
     * 自动在上下文中获取，如果没有则创建
     *
     * @return
     * @see com.i360day.invoker.HttpInvokerClientFactoryBean#getDecoder
     */
    Class<? extends Decoder> decoder() default Decoder.class;

    /**
     * 编码器
     * 自动在上下文中获取，如果没有则创建
     *
     * @return
     * @see com.i360day.invoker.HttpInvokerServiceExporter#afterPropertiesSet
     */
    Class<? extends Encoder> encoder() default Encoder.class;

    /**
     * 接口调用出错后，回调类
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    Class<?> fallback() default void.class;

    /**
     * 回调类工厂
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    Class<? extends FallbackFactory> fallbackFactory() default DefaultFallbackFactory.class;

    /**
     * 如果address不等于空，则向这个地址发送HTTP请求
     * 提供者-访问的根路径地址 其中IP部分则是"服务名称"
     * 例: http://invoker-service:9090/${server.servlet.context-path}
     * 例: https://invoker-service:9090/${server.servlet.context-path}
     * 如果为多个则使用轮询方式
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    String[] address() default {};

    /**
     * 如果出现多个同样的bean，设置该bean为默认
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    boolean primary() default true;

    /**
     * 分组
     * 如：v1、v2、v3...
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    String group() default "";

    /**
     * 版本号
     * 如：1.0.0
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    String version() default "";

}
