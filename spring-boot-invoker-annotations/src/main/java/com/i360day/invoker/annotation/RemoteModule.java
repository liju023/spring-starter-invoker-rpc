package com.i360day.invoker.annotation;

import java.lang.annotation.*;


/**
 * @description: rpc客户端模块
 * @author: 胡.青牛
 * @date: 2019/4/27 0027  15:19
 **/
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface RemoteModule {
    /**
     * 如果address不等于空，则向这个地址发送HTTP/redis/rabbitmq/websocket请求
     * 提供者-访问的根路径地址
     *
     * spring boot
     * 例: http://127.0.0.1:9090/${server.servlet.context-path}
     * 例: https://127.0.0.1:9090/${server.servlet.context-path}
     *
     * spring cloud
     * 例: http://127.0.0.1:9090/${server.servlet.context-path}/${spring.invoker.web-socket.endpoint}
     * 例: https://127.0.0.1:9090/${server.servlet.context-path}/${spring.invoker.web-socket.endpoint}
     *
     * rabbitmq
     * 例: amqp://127.0.0.1:5671 如果是集群模式则：”amqp://127.0.0.1:5671;amqp://127.0.0.1:5672;amqp://127.0.0.1:5673“ 英文”;“隔开
     *
     * redis
     * 例: redis://127.0.0.1:6379 如果是集群模式则：”redis://127.0.0.1:6377;redis://127.0.0.1:6378;redis://127.0.0.1:6379“ 英文”;“隔开
     *
     * @return
     */
    String[] address() default {};

    /**
     * 客户端请求的-账号
     *
     * @return
     */
    String clientUser() default "";

    /**
     * 客户端请求的-密码
     *
     * @return
     */
    String clientPassword() default "";
}
