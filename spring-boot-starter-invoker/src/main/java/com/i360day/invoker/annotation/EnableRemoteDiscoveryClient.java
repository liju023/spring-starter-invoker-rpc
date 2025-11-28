package com.i360day.invoker.annotation;

import com.i360day.invoker.registry.EnableRemoteDiscoveryClientImportSelector;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 启动远程客户端注册
 *
 * @author: 胡.青牛
 * @date: 2019/4/28 0028  10:27
 **/
@Inherited
@Documented
@RemoteScan
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({EnableRemoteDiscoveryClientImportSelector.class})
public @interface EnableRemoteDiscoveryClient {
    /**
     * <p> @Description: 扫描包路径，如果没有则使用（ComponentScan的包路径） <p>
     *
     * <p> @author: 胡.青牛 <p>
     *
     * <p> @Date:   2019/6/3 0003 12:06 <p>
     *
     * <p> @param null   </p>
     *
     * <p> @return:    <p>
     **/
    @AliasFor(annotation = RemoteScan.class)
    String[] basePackages() default {};
}

