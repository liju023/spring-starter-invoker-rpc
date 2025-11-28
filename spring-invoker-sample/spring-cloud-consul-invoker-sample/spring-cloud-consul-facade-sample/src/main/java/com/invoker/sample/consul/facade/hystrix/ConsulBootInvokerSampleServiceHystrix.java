package com.invoker.sample.consul.facade.hystrix;

import com.invoker.sample.consul.facade.ConsulInvokerSampleUserServiceFacade;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;

import java.util.Arrays;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/7/22 0022  16:11<p>
 **/
//@Component
public class ConsulBootInvokerSampleServiceHystrix implements ConsulInvokerSampleUserServiceFacade {
    @Override
    public SpringCloudSampleUserDto testQuery() {
        return new SpringCloudSampleUserDto(Arrays.asList("error"), "调用失败");
    }
}
