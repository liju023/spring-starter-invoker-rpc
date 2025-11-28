package com.i360day.invoker.security;


import com.i360day.invoker.support.RemoteInvocation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求拦截器
 * @author liju.z
 */
public interface InvokerSecurityAdapter {
    /**
     *  检查请求
     * @param request
     * @param response
     */
    void check(HttpServletRequest request, HttpServletResponse response, RemoteInvocation remoteInvocation);
}
