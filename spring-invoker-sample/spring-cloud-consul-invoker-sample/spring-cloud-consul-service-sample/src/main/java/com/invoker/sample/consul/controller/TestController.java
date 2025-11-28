package com.invoker.sample.consul.controller;

import com.invoker.sample.consul.facade.ConsulInvokerSampleUserServiceFacade;
import com.invoker.sample.consul.facade.dto.SpringCloudSampleUserDto;
import com.invoker.sample.consul.service.SpringBootInvokerSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    private SpringBootInvokerSampleService springInvokerSampleService;

    @Autowired
    private ConsulInvokerSampleUserServiceFacade consulInvokerSampleUserServiceFacade;

    @RequestMapping("/query")
    public SpringCloudSampleUserDto query(){
        return springInvokerSampleService.testQuery(new SpringCloudSampleUserDto());
    }

    @GetMapping("/user")
    public SpringCloudSampleUserDto getUser(){
        return consulInvokerSampleUserServiceFacade.testQuery();
    }

}
