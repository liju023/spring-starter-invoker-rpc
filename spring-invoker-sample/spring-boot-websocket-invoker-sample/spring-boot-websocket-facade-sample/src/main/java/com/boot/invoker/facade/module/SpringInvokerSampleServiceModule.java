package com.boot.invoker.facade.module;

import com.i360day.invoker.annotation.RemoteModule;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/7/22 0022  15:26<p>
 **/
@RemoteModule(address = "http://127.0.0.1:9092/invoker-server")
public interface SpringInvokerSampleServiceModule {

    default String defaultTest(){
        return "调用父类";
    }
}

