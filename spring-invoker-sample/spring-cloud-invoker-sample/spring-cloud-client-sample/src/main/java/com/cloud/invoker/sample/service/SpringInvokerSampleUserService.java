package com.cloud.invoker.sample.service;


import com.cloud.invoker.sample.facade.SpringInvokerSampleUserServiceFacade;
import com.cloud.invoker.sample.facade.dto.SpringCloudSampleUserDto;
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
public class SpringInvokerSampleUserService implements SpringInvokerSampleUserServiceFacade {


    public SpringCloudSampleUserDto testQuery() {
        return new SpringCloudSampleUserDto(Arrays.asList("success"), "success");
    }
}
