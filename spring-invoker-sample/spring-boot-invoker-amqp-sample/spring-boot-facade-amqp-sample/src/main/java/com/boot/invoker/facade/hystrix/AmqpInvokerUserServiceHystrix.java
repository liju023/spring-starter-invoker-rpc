package com.boot.invoker.facade.hystrix;

import com.boot.invoker.facade.AmqpInvokerUserServiceFacade;
import com.boot.invoker.facade.vo.TestVo;
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
public class AmqpInvokerUserServiceHystrix implements FallbackFactory<AmqpInvokerUserServiceFacade> {

    @Override
    public AmqpInvokerUserServiceFacade create(Throwable throwable) {
        return new AmqpInvokerUserServiceFacade(){

            @Override
            public String testQuery(TestVo testVo) {
                return "请求错误";
            }
        };
    }
}
