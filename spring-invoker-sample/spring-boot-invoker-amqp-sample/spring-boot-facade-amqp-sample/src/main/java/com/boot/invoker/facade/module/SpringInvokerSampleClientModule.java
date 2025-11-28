package com.boot.invoker.facade.module;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/7/22 0022  15:26<p>
 **/
public interface SpringInvokerSampleClientModule {

    default String defaultTest(){
        return "调用父类";
    }
}

