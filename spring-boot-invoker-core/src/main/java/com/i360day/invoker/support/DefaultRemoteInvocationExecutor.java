/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i360day.invoker.support;

import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.exception.InvalidAcceptTimestampException;
import com.i360day.invoker.exception.InvalidSignatureException;
import com.i360day.invoker.security.InvokerSecurityAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of the {@link RemoteInvocationExecutor} interface.
 * Simply delegates to {@link RemoteInvocation}'s invoke method.
 *
 * @author Juergen Hoeller
 * @see RemoteInvocation#invoke
 * @since 1.1
 */
public class DefaultRemoteInvocationExecutor implements RemoteInvocationExecutor {
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteInvocationExecutor.class);
    /**
     * 安全适配器
     */
    private final InvokerSecurityAdapter invokerSecurityAdapter;
    /**
     * accept 接受值
     */
    private MediaType acceptMediaType = MediaType.valueOf(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);

    public DefaultRemoteInvocationExecutor(InvokerSecurityAdapter invokerSecurityAdapter) {
        this.invokerSecurityAdapter = invokerSecurityAdapter;
    }

    /**
     * 检查头信息
     *
     * @param request
     * @throws HttpMediaTypeNotAcceptableException
     */
    private void checkAccept(HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotAcceptableException {
        Enumeration<String> headers = request.getHeaders(HttpHeaders.ACCEPT);
        List<String> headerValues = new LinkedList<>();
        while (headers.hasMoreElements()) {
            headerValues.add(headers.nextElement());
        }
        List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerValues);
        MediaType.sortBySpecificity(mediaTypes);
        if (!mediaTypes.contains(acceptMediaType)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            throw new HttpMediaTypeNotAcceptableException("Could not parse 'Accept' header " + headerValues + ": accept is " + acceptMediaType);
        }
    }

    /**
     * 验证时间有效性
     *
     * @param request
     * @param response
     */
    private void verifyTimestamp(HttpServletRequest request, HttpServletResponse response) {
        String acceptTimestamp = request.getHeader(InvokerConstant.ACCEPT_TIMESTAMP);
        if (ObjectUtils.isEmpty(acceptTimestamp)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            throw new InvalidAcceptTimestampException(String.format("Invalid %s", InvokerConstant.ACCEPT_TIMESTAMP));
        }
        //验证当前请求是否在有效时间内
        long requestTimestamp = Long.valueOf(acceptTimestamp);
        if ((System.currentTimeMillis() - (5 * 1000 * 60)) > requestTimestamp) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            throw new InvalidAcceptTimestampException(String.format("Request time has expired %s", requestTimestamp));
        }
    }

    /**
     * 验证签名
     *
     * @param request
     * @param response
     * @param invocation
     */
    private void signVerify(HttpServletRequest request, HttpServletResponse response, RemoteInvocation invocation, Object service) throws NoSuchMethodException {
        Method method = service.getClass().getMethod(invocation.getMethodName(), invocation.getParameterClazzs());
//		Method method = invocation.getMethod(service);
//        String signValue = HttpInvokerRemoteConstant.sign(invocation.getMethodName(), method.getReturnType(), invocation.getArguments());
        String signValue = InvokerConstant.sign(method, invocation.getArguments());
        if (!ObjectUtils.isEquals(signValue, request.getHeader(InvokerConstant.HTTP_INVOKER_AUTHORIZATION_KEY))) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            throw new InvalidSignatureException("Invalid signature " + signValue);
        }
    }

    @Override
    public Object invoke(RemoteInvocation invocation, Object targetObject, Class<?> targetInterface) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Assert.notNull(invocation, "RemoteInvocation must not be null");
        Assert.notNull(targetObject, "Target object must not be null");
        try {
            //http
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if(requestAttributes != null && requestAttributes instanceof ServletRequestAttributes){
                ServletRequestAttributes servletRequestAttributes = ( ServletRequestAttributes )requestAttributes;
                HttpServletRequest request = servletRequestAttributes.getRequest();
                HttpServletResponse response = servletRequestAttributes.getResponse();
                //验证请求时间有效性
                verifyTimestamp(request, response);
                //接受头信息
                checkAccept(request, response);
                //sign verify
//                signVerify(request, response, invocation, targetObject);
                //check
                invokerSecurityAdapter.check(request, response, invocation);
            }

            return invocation.invoke(targetObject);
        } catch (HttpMediaTypeNotAcceptableException exception) {
            throw new InvocationTargetException(exception);
        }
    }
}
