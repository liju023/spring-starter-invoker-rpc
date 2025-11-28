package com.invoker.sample.nacos.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: spring-cloud-invoker-parent
 * @description:
 * @author: liju.z
 * @create: 2021-12-05 14:04
 **/
@FeignClient(contextId = "FeignServiceFacade", value = "invoker-service", fallbackFactory = FeignServiceFacadeHystrix.class)
public interface FeignServiceFacade {

    @GetMapping("/test/query")
    String testQuery();
}
