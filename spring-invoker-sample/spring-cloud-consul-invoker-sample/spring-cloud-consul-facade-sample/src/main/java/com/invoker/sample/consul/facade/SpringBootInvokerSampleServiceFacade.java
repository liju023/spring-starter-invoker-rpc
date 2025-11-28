package com.invoker.sample.consul.facade;

import com.i360day.invoker.annotation.RemoteClient;
import com.invoker.sample.consul.facade.factory.SpringBootInvokerSampleServiceFallbackFactory;
import com.invoker.sample.consul.facade.module.SpringBootInvokerSampleServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteClient(fallbackFactory = SpringBootInvokerSampleServiceFallbackFactory.class)
public interface SpringBootInvokerSampleServiceFacade extends SpringBootInvokerSampleServiceModule {

    String testQuery();

}
