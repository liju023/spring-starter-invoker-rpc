package com.invoker.sample.nacos.facade;

import com.i360day.invoker.annotation.RemoteClient;
import com.invoker.sample.nacos.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.nacos.facade.factory.SpringBootInvokerSampleServiceHystrix;
import com.invoker.sample.nacos.facade.module.SpringInvokerSampleUserServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteClient(fallback = SpringBootInvokerSampleServiceHystrix.class)
public interface SpringBootInvokerSampleUserServiceFacade extends SpringInvokerSampleUserServiceModule {

    SpringCloudSampleUserDto testQuery();
}
