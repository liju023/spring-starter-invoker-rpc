package com.invoker.sample.nacos.feign;


import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @program: spring-cloud-invoker-parent
 * @description:
 * @author: liju.z
 * @create: 2021-12-14 21:44
 **/
@Component
public class FeignServiceFacadeHystrix implements FallbackFactory<FeignServiceFacade> {

    @Override
    public FeignServiceFacade create(Throwable throwable) {
        return () -> "调用失败";
    }
}
