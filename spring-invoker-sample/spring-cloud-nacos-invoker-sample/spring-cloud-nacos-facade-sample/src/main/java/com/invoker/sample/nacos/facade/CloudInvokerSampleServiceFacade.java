package com.invoker.sample.nacos.facade;

import com.i360day.invoker.annotation.RemoteCloudClient;
import com.invoker.sample.nacos.facade.factory.CloudInvokerSampleServiceHystrix;
import com.invoker.sample.nacos.facade.module.SpringBootInvokerSampleServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteCloudClient(fallbackFactory = CloudInvokerSampleServiceHystrix.class)
public interface CloudInvokerSampleServiceFacade extends SpringBootInvokerSampleServiceModule {

    String testQuery();

}
