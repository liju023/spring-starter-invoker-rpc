package com.boot.invoker.facade.hystrix;

import com.boot.invoker.facade.SpringBootInvokerSampleServerFacade;
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
public class AmqpInvokerSampleServiceHystrix implements FallbackFactory<SpringBootInvokerSampleServerFacade> {

    @Override
    public SpringBootInvokerSampleServerFacade create(Throwable throwable) {
        throwable.printStackTrace();
        return new SpringBootInvokerSampleServerFacade(){

            @Override
            public Object query() {
                return "请求错误";
            }
        };
    }
}
