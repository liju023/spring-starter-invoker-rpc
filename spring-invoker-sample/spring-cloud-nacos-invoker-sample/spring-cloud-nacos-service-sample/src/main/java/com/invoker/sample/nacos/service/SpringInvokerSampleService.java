package com.invoker.sample.nacos.service;


import com.invoker.sample.nacos.facade.CloudInvokerSampleServiceFacadeV2;
import com.invoker.sample.nacos.facade.SpringInvokerSampleServiceFacade;
import com.invoker.sample.nacos.facade.dto.SpringCloudSampleUserDto;
import org.springframework.stereotype.Service;


/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  13:26<p>
 **/
@Service
public class SpringInvokerSampleService implements SpringInvokerSampleServiceFacade, CloudInvokerSampleServiceFacadeV2 {


    @Override
    public SpringCloudSampleUserDto testQuery(SpringCloudSampleUserDto springCloudSampleUserDto){
         System.out.println("client is request" + (1 / springCloudSampleUserDto.getCount()));
//        return new SpringCloudSampleUserDto(Arrays.asList("1"), "success");
        return springCloudSampleUserDto;
    }
}
