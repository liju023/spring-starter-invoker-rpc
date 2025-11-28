/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.i360day.invoker.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i360day.invoker.common.InvokerConstant;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;

/**
 * 自定义application/x-rpc-http-invoker，默认使用json
 *
 * @author liju.z
 * @date 2025/4/16 20:19
 */
public class HttpInvokerHttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    /**
     * 只接受的content-type
     */
    private final MediaType acceptMediaType = MediaType.parseMediaType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);

    /**
     * create AbstractJackson2HttpMessageConverter
     *
     * @param objectMapper
     */
    public HttpInvokerHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.parseMediaType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER));
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return canRead(contextClass, mediaType);
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return this.acceptMediaType.equals(mediaType);
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return canWrite(clazz, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return this.acceptMediaType.equals(mediaType);
    }
}
