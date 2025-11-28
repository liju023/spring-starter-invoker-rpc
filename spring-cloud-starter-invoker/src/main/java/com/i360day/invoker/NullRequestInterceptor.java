package com.i360day.invoker;

import com.i360day.invoker.interceptor.InvokerRequestInterceptor;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/6/22 0022  11:46<p>
 **/
public final class NullRequestInterceptor implements InvokerRequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
//        requestTemplate.copyAuthorizationToken();
    }
}
