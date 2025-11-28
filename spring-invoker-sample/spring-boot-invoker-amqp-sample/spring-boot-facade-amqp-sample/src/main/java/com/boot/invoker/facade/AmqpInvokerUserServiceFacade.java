package com.boot.invoker.facade;

import com.boot.invoker.facade.hystrix.AmqpInvokerUserServiceHystrix;
import com.boot.invoker.facade.module.AmqpInvokerSampleServiceModule;
import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.annotation.RemoteAmqpClient;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>fallback = SpringEurekaEurekaInvokerSampleServiceHystrix.class,
 **/
@RemoteAmqpClient(address = "amqp://47.92.170.164:25672/invoker-server", username = "ryb", password = "Ryb@2021", virtualHost = "/ryb-data", fallbackFactory = AmqpInvokerUserServiceHystrix.class)
public interface AmqpInvokerUserServiceFacade extends AmqpInvokerSampleServiceModule {

    /**
     * 测试查询
     *
     * @param testVo
     * @return
     */
    String testQuery(TestVo testVo);
}
