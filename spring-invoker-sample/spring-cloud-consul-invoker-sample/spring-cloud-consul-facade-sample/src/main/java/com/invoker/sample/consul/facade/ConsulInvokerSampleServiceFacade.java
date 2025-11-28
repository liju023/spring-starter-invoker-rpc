package com.invoker.sample.consul.facade;

import com.i360day.invoker.annotation.RemoteCloudClient;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.consul.facade.factory.SpringBootInvokerSampleFacadeFactory;
import com.invoker.sample.consul.facade.module.ConsulInvokerSampleServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteCloudClient(fallbackFactory = SpringBootInvokerSampleFacadeFactory.class, group = "group", version = "0.0.1")
public interface ConsulInvokerSampleServiceFacade extends ConsulInvokerSampleServiceModule {

    SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto);
}
