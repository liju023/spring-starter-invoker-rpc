package com.invoker.sample.nacos.controller;

import com.invoker.sample.nacos.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.nacos.service.SpringInvokerSampleService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private SpringInvokerSampleService springInvokerSampleService;

    @RequestMapping("/query")
    public SpringCloudSampleUserDto query(){
        return springInvokerSampleService.testQuery(new SpringCloudSampleUserDto());
    }
}
