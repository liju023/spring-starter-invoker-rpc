package com.invoker.sample.consul.service;


import com.invoker.sample.consul.facade.ConsulInvokerSampleUserServiceFacade;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  13:26<p>
 **/
@Service
public class SpringBootInvokerSampleUserService implements ConsulInvokerSampleUserServiceFacade {


    public SpringCloudSampleUserDto testQuery(){
        return new SpringCloudSampleUserDto(Arrays.asList("success"), "success");
    }
}
