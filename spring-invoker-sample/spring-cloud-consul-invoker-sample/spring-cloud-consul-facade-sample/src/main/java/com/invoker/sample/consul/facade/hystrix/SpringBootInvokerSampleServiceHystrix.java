package com.invoker.sample.consul.facade.hystrix;

import com.invoker.sample.consul.facade.SpringBootInvokerSampleServiceFacadeV2;
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
public class SpringBootInvokerSampleServiceHystrix implements SpringBootInvokerSampleServiceFacadeV2 {
    @Override
    public SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto) {
        return new SpringCloudSampleUserDto(Arrays.asList("error"), "调用失败");
    }
}
