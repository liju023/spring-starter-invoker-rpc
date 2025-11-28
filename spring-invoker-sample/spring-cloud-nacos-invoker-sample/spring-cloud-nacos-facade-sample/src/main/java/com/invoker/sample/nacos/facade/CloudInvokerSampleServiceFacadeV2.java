package com.invoker.sample.nacos.facade;

import com.i360day.invoker.annotation.RemoteCloudClient;
import com.invoker.sample.nacos.aspect.TestAnnotation;
import com.invoker.sample.nacos.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.nacos.facade.factory.SpringBootInvokerSampleFacadeFactory;
import com.invoker.sample.nacos.facade.module.SpringInvokerSampleServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
//@RemoteClient(fallback = SpringInvokerSampleServiceHystrix.class)
@RemoteCloudClient(fallbackFactory = SpringBootInvokerSampleFacadeFactory.class, group = "group", version = "0.0.2")
public interface CloudInvokerSampleServiceFacadeV2 extends SpringInvokerSampleServiceModule {

    @TestAnnotation
    SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto);
}
