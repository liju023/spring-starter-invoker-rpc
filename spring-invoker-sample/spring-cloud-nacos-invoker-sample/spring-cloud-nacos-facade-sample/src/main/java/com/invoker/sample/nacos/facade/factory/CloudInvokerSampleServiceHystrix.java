package com.invoker.sample.nacos.facade.factory;

import com.i360day.invoker.hystrix.FallbackFactory;
import com.invoker.sample.nacos.facade.CloudInvokerSampleServiceFacade;
import org.springframework.stereotype.Component;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/7/22 0022  16:11<p>
 **/
@Component
public class CloudInvokerSampleServiceHystrix implements FallbackFactory {
//    @Override
//    public String testQuery() {
//        return null;
//    }

    @Override
    public Object create(Throwable var1) {
        CloudInvokerSampleServiceFacade serviceFacade = (CloudInvokerSampleServiceFacade) () -> null;
        return serviceFacade;
    }
}
