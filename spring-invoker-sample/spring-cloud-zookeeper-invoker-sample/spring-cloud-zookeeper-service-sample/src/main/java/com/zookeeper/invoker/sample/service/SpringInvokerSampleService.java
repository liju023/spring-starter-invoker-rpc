package com.zookeeper.invoker.sample.service;


import com.zookeeper.invoker.sample.facade.SpringInvokerSampleServiceFacade;
import com.zookeeper.invoker.sample.facade.dto.SpringCloudSampleUserDto;
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
public class SpringInvokerSampleService implements SpringInvokerSampleServiceFacade {


    @Override
    public SpringCloudSampleUserDto testQuery(){
        return new SpringCloudSampleUserDto(Arrays.asList("1"), "success");
    }
}
