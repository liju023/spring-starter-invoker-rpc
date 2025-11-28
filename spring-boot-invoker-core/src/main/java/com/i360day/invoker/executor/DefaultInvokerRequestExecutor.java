/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i360day.invoker.executor;

import com.i360day.invoker.MethodMetadata;
import com.i360day.invoker.RequestTemplate;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.interceptor.InvokerRequestInterceptor;
import com.i360day.invoker.request.InvokerRequest;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

/**
 * <p> @Description: 远程请求类 <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @Date:   2019/6/24 0024 10:45 <p>
 *
 * <p> @param null   </p>
 *
 * <p> @return:    <p>
 **/
class DefaultInvokerRequestExecutor extends AbstractInvokerRequestExecutor {
    private final InvokerRequest invokerRequest;
    private final Collection<InvokerRequestInterceptor> requestInterceptors;

    /**
     * create Request Executor
     *
     * @param invokerRequest
     * @param requestInterceptors
     */
    public DefaultInvokerRequestExecutor(InvokerRequest invokerRequest, Collection<InvokerRequestInterceptor> requestInterceptors) {
        this.invokerRequest = invokerRequest;
        this.requestInterceptors = requestInterceptors;
    }

    @Override
    protected Response doExecuteRequest(RequestTemplate requestTemplate) throws IOException, ClassNotFoundException {
        //请求之前
        for (InvokerRequestInterceptor requestInterceptor : requestInterceptors) {
            requestInterceptor.apply(requestTemplate);
        }

        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        if (localeContext != null) {
            Locale locale = localeContext.getLocale();
            if (locale != null) {
                requestTemplate.addHeader(HTTP_HEADER_ACCEPT_LANGUAGE, locale.toLanguageTag());
            }
        }
        //设置GZIP头
        if (isAcceptGzipEncoding()) {
            requestTemplate.addHeader(HTTP_HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
        }
        //请求头
        requestTemplate.setContentType(getContentType());
        //rpc 协议头
        requestTemplate.addHeader(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER_KEY, InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);
        //请求时间戳
        requestTemplate.addHeader(InvokerConstant.ACCEPT_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        //sign
        MethodMetadata methodMetadata = requestTemplate.getMethodMetadata();
        requestTemplate.addHeader(InvokerConstant.HTTP_INVOKER_AUTHORIZATION_KEY, InvokerConstant.sign(methodMetadata.getMethod(), methodMetadata.getArgs()));
        //execute
        return invokerRequest.executor(requestTemplate);
    }
}
