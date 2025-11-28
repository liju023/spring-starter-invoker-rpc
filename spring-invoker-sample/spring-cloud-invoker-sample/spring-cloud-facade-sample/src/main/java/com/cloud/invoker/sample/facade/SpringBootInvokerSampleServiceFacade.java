package com.cloud.invoker.sample.facade;

import com.cloud.invoker.sample.facade.module.SpringBootInvokerSampleServiceModule;
import com.i360day.invoker.annotation.RemoteClient;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteClient(fallbackFactory = SpringBootInvokerSampleServiceHystrix.class)
public interface SpringBootInvokerSampleServiceFacade extends SpringBootInvokerSampleServiceModule {

    String testQuery();

}
