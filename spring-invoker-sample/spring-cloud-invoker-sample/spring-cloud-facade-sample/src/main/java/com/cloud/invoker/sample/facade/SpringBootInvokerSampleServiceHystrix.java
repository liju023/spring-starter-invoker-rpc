package com.cloud.invoker.sample.facade;

import com.i360day.invoker.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/7/22 0022  16:11<p>
 **/
@Component
public class SpringBootInvokerSampleServiceHystrix implements FallbackFactory {
//    @Override
//    public String testQuery() {
//        return null;
//    }

    @Override
    public Object create(Throwable var1) {
        SpringBootInvokerSampleServiceFacade serviceFacade = (SpringBootInvokerSampleServiceFacade) () -> null;
        return serviceFacade;
    }
}
