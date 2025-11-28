package com.i360day.invoker.interceptor;

import com.i360day.invoker.RequestTemplate;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/6/22 0022  10:44<p>
 **/
public interface InvokerRequestInterceptor {
    /**
     * 对requestTemplate进行设置
     *
     * @param requestTemplate
     */
    void apply(RequestTemplate requestTemplate);
}
