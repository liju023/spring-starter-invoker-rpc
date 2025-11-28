package com.boot.invoker.facade;

import com.boot.invoker.facade.hystrix.AmqpInvokerSampleServiceHystrix;
import com.boot.invoker.facade.module.SpringInvokerSampleClientModule;
import com.i360day.invoker.annotation.RemoteClient;

@RemoteClient(address = "http://127.0.0.1:9091", fallbackFactory = AmqpInvokerSampleServiceHystrix.class)
public interface SpringBootInvokerSampleServerFacade extends SpringInvokerSampleClientModule {

    Object query();
}
