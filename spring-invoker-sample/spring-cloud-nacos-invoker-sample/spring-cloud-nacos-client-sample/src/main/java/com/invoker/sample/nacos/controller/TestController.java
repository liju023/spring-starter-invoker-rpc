package com.invoker.sample.nacos.controller;

import com.invoker.sample.nacos.facade.CloudInvokerSampleServiceFacadeV2;
import com.invoker.sample.nacos.facade.SpringInvokerSampleServiceFacade;
import com.invoker.sample.nacos.facade.dto.SpringCloudSampleUserDto;
//import com.invoker.sample.nacos.feign.FeignServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:09<p>
 **/
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SpringInvokerSampleServiceFacade springInvokerSampleService;

    @Autowired
    private CloudInvokerSampleServiceFacadeV2 springInvokerSampleServiceFacadeV2;

//    @Autowired
//    private FeignServiceFacade feignServiceFacade;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping("/query")
    public SpringCloudSampleUserDto query(SpringCloudSampleUserDto springCloudSampleUserDto){
        SpringCloudSampleUserDto springCloudSampleUserDto1 = springInvokerSampleServiceFacadeV2.testQuery(springCloudSampleUserDto);
        return springInvokerSampleService.testQuery(springCloudSampleUserDto1);
    }

//    @RequestMapping("/feign")
//    public String feign(){
//
//            return feignServiceFacade.testQuery();
//    }

}
