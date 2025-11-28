package com.invoker.sample.consul.facade;

import com.i360day.invoker.annotation.RemoteCloudClient;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.consul.facade.hystrix.ConsulBootInvokerSampleServiceHystrix;
import com.invoker.sample.consul.facade.module.SpringBootInvokerSampleUserServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteCloudClient(fallback = ConsulBootInvokerSampleServiceHystrix.class)
public interface ConsulInvokerSampleUserServiceFacade extends SpringBootInvokerSampleUserServiceModule {

    SpringCloudSampleUserDto testQuery();
}
