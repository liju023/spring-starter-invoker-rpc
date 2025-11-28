package com.invoker.sample.nacos.facade.factory;

import com.i360day.invoker.hystrix.FallbackFactory;
import com.invoker.sample.nacos.facade.SpringInvokerSampleServiceFacade;
import com.invoker.sample.nacos.facade.dto.SpringCloudSampleUserDto;

import java.util.Arrays;

/**
 * @program: spring-cloud-invoker-parent
 * @description:
 * @author: liju.z
 * @create: 2022-11-12 11:06
 **/
//@Component
public class SpringBootInvokerSampleFacadeFactory implements FallbackFactory<SpringInvokerSampleServiceFacade> {
    @Override
    public SpringInvokerSampleServiceFacade create(Throwable throwable) {
        return new SpringInvokerSampleServiceFacade(){

            @Override
            public SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto) {
                return new SpringCloudSampleUserDto(Arrays.asList("fail"), "fail");
            }
        };
    }
}
