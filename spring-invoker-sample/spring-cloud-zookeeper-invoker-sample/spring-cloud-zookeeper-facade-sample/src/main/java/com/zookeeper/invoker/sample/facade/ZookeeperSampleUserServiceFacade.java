package com.zookeeper.invoker.sample.facade;

import com.i360day.invoker.annotation.RemoteCloudClient;
import com.zookeeper.invoker.sample.facade.dto.SpringCloudSampleUserDto;
import com.zookeeper.invoker.sample.facade.hystrix.ZookeeperSampleServiceHystrix;
import com.zookeeper.invoker.sample.facade.module.ZookeeperInvokerSampleUserServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteCloudClient(fallback = ZookeeperSampleServiceHystrix.class)
public interface ZookeeperSampleUserServiceFacade extends ZookeeperInvokerSampleUserServiceModule {

    SpringCloudSampleUserDto testQuery();
}
