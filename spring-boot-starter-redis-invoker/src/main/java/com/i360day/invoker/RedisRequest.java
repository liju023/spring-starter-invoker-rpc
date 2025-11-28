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
package com.i360day.invoker;

import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author liju.z
 * @date 2024/3/30 18:09
 */
public class RedisRequest implements Serializable {
    private String requestId;
    private byte[] body;
    private String replyTo;
    private int readTimeout;
    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Map<String, Collection<String>> getHeaders() {
        return Optional.ofNullable(this.headers).orElseGet(() -> new LinkedHashMap<>());
    }

    public void setHeaders(Map<String, Collection<String>> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, Collection<String> value) {
        if(this.headers == null){
            this.headers = new LinkedHashMap<>();
        }
        this.headers.put(key, value);
    }

    public String getHeader(String key){
        return Optional.ofNullable(getHeaders().get(key)).orElse(Collections.emptyList()).iterator().next();
    }

    public Collection<String> getHeaders(String key){
        return getHeaders().get(key);
    }

    public static Buildler builder() {
        return new Buildler(new RedisRequest());
    }

    public static RedisRequest convertRedisRequest(String replyTo, RequestTemplate requestTemplate) {
        String requestId = DigestUtils.md5DigestAsHex(
                String.format("%s_%s_%s", requestTemplate.getRequestPath(), System.currentTimeMillis(), UUID.randomUUID()).getBytes()
        );
        return RedisRequest.builder()
                .requestId(requestId)
                .body(requestTemplate.getBodyTemplate().getBody())
                .replyTo(replyTo)
                .header(requestTemplate.getHeaders())
                .build();
    }

    public static class Buildler {
        private RedisRequest request;

        public Buildler(RedisRequest request) {
            this.request = request;
        }

        public Buildler body(byte[] bytes) {
            request.setBody(bytes);
            return this;
        }

        public Buildler requestId(String requestId) {
            request.setRequestId(requestId);
            return this;
        }

        public Buildler readTimeout(int readTimeout){
            request.setReadTimeout(readTimeout);
            return this;
        }

        public Buildler replyTo(String replyTo) {
            request.setReplyTo(replyTo);
            return this;
        }

        public void header(String key, Collection<String> value) {
            request.addHeader(key, value);
        }

        public Buildler header(Map<String, Collection<String>> header) {
            request.setHeaders(header);
            return this;
        }

        public RedisRequest build() {
            return request;
        }
    }
}
