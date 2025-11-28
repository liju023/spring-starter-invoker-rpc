package com.invoker.sample.consul.facade;

import com.i360day.invoker.annotation.RemoteClient;
import com.invoker.sample.consul.aspect.TestAnnotation;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.consul.facade.hystrix.SpringBootInvokerSampleServiceHystrix;
import com.invoker.sample.consul.facade.module.SpringBootInvokerSampleServiceModule;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>
 **/
//@RemoteClient(fallback = SpringInvokerSampleServiceHystrix.class)
@RemoteClient(fallback = SpringBootInvokerSampleServiceHystrix.class, group = "group", version = "0.0.2")
public interface SpringBootInvokerSampleServiceFacadeV2 extends SpringBootInvokerSampleServiceModule {

    @TestAnnotation
    SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto);
}
