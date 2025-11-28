package com.invoker.sample.consul.facade.factory;

import com.i360day.invoker.hystrix.FallbackFactory;
import com.invoker.sample.consul.facade.ConsulInvokerSampleServiceFacade;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;

import java.util.Arrays;

/**
 * @program: spring-cloud-invoker-parent
 * @description:
 * @author: liju.z
 * @create: 2022-11-12 11:06
 **/
//@Component
public class SpringBootInvokerSampleFacadeFactory implements FallbackFactory<ConsulInvokerSampleServiceFacade> {
    @Override
    public ConsulInvokerSampleServiceFacade create(Throwable throwable) {
        return new ConsulInvokerSampleServiceFacade(){

            @Override
            public SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto) {
                return new SpringCloudSampleUserDto(Arrays.asList("fail"), "fail");
            }
        };
    }
}
