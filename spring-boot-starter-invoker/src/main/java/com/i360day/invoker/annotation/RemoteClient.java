package com.i360day.invoker.annotation;

import com.i360day.invoker.HttpInvokerClientFactoryBean;
import com.i360day.invoker.HttpInvokerServiceExporter;
import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.codes.encoder.Encoder;
import com.i360day.invoker.hystrix.DefaultFallbackFactory;
import com.i360day.invoker.hystrix.FallbackFactory;
import com.i360day.invoker.support.RemoteExporter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * rpc客户端
 *
 * @author: 胡.青牛
 * @date: 2019/4/27 0027  15:19
 **/
@Inherited
@Documented
@RemoteModule
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface RemoteClient {
    /**
     * 客户端代理类
     *
     * @return
     */
    Class<? extends FactoryBean> clientProxyClass() default HttpInvokerClientFactoryBean.class;

    /**
     * 服务端代理类
     *
     * @return
     */
    Class<? extends RemoteExporter> serverProxyClass() default HttpInvokerServiceExporter.class;

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
    Class<?> fallback() default void.class;

    /**
     * 回调类工厂
     *
     * @return
     */
    Class<? extends FallbackFactory> fallbackFactory() default DefaultFallbackFactory.class;

    /**
     * 如果address不等于空，则向这个地址发送HTTP请求
     * 提供者-访问的根路径地址
     * 例: http://127.0.0.1:9090/${server.servlet.context-path}
     * 例: https://127.0.0.1:9090/${server.servlet.context-path}
     * 如果为多个则使用轮询方式
     *
     * @return
     */
    @AliasFor(annotation = RemoteModule.class)
    String[] address() default {};

    /**
     * 如果出现多个同样的bean，设置该bean为默认
     *
     * @return
     */
    boolean primary() default true;

    /**
     * 分组
     * 如：v1、v2、v3...
     *
     * @return
     */
    String group() default "";

    /**
     * 版本号
     * 如：1.0.0
     *
     * @return
     */
    String version() default "";
}
