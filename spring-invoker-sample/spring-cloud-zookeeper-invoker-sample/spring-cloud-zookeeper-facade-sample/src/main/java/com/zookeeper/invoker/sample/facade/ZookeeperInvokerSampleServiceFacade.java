package com.zookeeper.invoker.sample.facade;

import com.i360day.invoker.annotation.RemoteCloudClient;
import com.zookeeper.invoker.sample.facade.hystrix.SpringBootInvokerSampleServiceHystrix;
import com.zookeeper.invoker.sample.facade.module.ZookeeperInvokerSampleServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteCloudClient(fallbackFactory = SpringBootInvokerSampleServiceHystrix.class)
public interface ZookeeperInvokerSampleServiceFacade extends ZookeeperInvokerSampleServiceModule {

    String testQuery();

}
