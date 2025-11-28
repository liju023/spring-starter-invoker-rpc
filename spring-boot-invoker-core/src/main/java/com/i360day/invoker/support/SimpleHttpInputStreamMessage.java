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
package com.i360day.invoker.support;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author liju.z
 * @date 2024/10/10 20:52
 */
public class SimpleHttpInputStreamMessage implements HttpInputMessage {

    private HttpServletRequest request;
    private MediaType mediaType;
    private InputStream inputStream;

    public SimpleHttpInputStreamMessage(HttpServletRequest request, MediaType mediaType) throws IOException {
        this.request = request;
        this.mediaType = mediaType;
    }

    public SimpleHttpInputStreamMessage(InputStream inputStream, MediaType mediaType) {
        this.inputStream = inputStream;
        this.mediaType = mediaType;
    }

    @Override
    public InputStream getBody() throws IOException {
        if(inputStream != null){
            return inputStream;
        }
        return request.getInputStream();
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(mediaType);
        return httpHeaders;
    }
}
