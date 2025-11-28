package com.cloud.invoker.sample.facade;

import com.cloud.invoker.sample.facade.dto.SpringCloudSampleUserDto;
import com.cloud.invoker.sample.facade.module.SpringInvokerSampleServiceModule;
import com.i360day.invoker.annotation.RemoteCloudClient;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
@RemoteCloudClient(fallback = SpringInvokerSampleServiceHystrix.class)
public interface SpringInvokerSampleServiceFacade extends SpringInvokerSampleServiceModule {

    SpringCloudSampleUserDto testQuery();
}
