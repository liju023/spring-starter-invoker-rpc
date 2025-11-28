package com.i360day.invoker.annotation;

import com.i360day.invoker.RedisInvokerClientFactoryBean;
import com.i360day.invoker.RedisInvokerServiceExporter;
import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.codes.encoder.Encoder;
import com.i360day.invoker.hystrix.DefaultFallbackFactory;
import com.i360day.invoker.hystrix.FallbackFactory;
import com.i360day.invoker.properties.ModeType;
import com.i360day.invoker.support.RemoteExporter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * 指定地址时只支持【spring-boot-starter-data-redis】这个包
 *
 * @description: rpc客户端
 * @author: 胡.青牛
 * @date: 2019/4/27 0027  15:19
 **/
@Inherited
@Documented
@RemoteClient
@RemoteModule
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface RemoteRedisClient {
    /**
     * 客户端代理类
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    Class<? extends FactoryBean> clientProxyClass() default RedisInvokerClientFactoryBean.class;

    /**
     * 服务端代理类
     *
     * @return
     */
    @AliasFor(annotation = RemoteClient.class)
    Class<? extends RemoteExporter> serverProxyClass() default RedisInvokerServiceExporter.class;

    /**
     * 解码器
     * 自动在上下文中获取，如果没有则创建
     *
     * @return
     * @see com.i360day.invoker.HttpInvokerClientFactoryBean#getDecoder
     */
    @AliasFor(annotation = RemoteClient.class)
    Class<? extends Decoder> decoder() default Decoder.class;

    /**
     * 编码器
     * 自动在上下文中获取，如果没有则创建
     *
     * @return
     * @see com.i360day.invoker.HttpInvokerServiceExporter#afterPropertiesSet
     */
    @AliasFor(annotation = RemoteClient.class)
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
     * 如果address不等于空，则向这个地址发送订阅请求
     * redis的IP和端口， 如果是集群模式则：”redis://127.0.0.1:6377;redis://127.0.0.1:6378;redis://127.0.0.1:6379“ 英文”;“隔开
     * 例: redis://127.0.0.1:6379
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

    /**
     * redis 账号
     *
     * @return
     */
    String username() default "";

    /**
     * redis 密码
     *
     * @return
     */
    String password() default "";

    /**
     * redis database
     *
     * @return
     */
    int database() default 0;

    /**
     * 最大消费
     *
     * @return
     */
    int prefetchCount() default 1000;

    /**
     * redis 消息模式
     *
     * @return
     */
    ModeType modeType() default ModeType.UNKNOWN;
}
