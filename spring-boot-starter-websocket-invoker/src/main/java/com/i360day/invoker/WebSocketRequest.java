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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author liju.z
 * @date 2024/3/30 18:09
 */
public class WebSocketRequest implements Serializable {
    private String requestId;
    private byte[] body;
    private String replyTo;
    private Map<String, Collection<String>> header = new LinkedHashMap<>();

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

    public Map<String, Collection<String>> getHeader() {
        return Optional.ofNullable(header).orElseGet(() -> new LinkedHashMap<>());
    }

    public void setHeaders(Map<String, Collection<String>> headers) {
        this.header = headers;
    }

    public void setHeader(Map<String, Collection<String>> header) {
        this.header = header;
    }

    public String getHeader(String key){
        Collection<String> collection = Optional.ofNullable(getHeader().get(key)).orElse(Collections.emptyList());
        if(collection.isEmpty()) return null;

        return collection.iterator().next();
    }

    public Collection<String> getHeaders(String key){
        return getHeader().get(key);
    }

    public void addHeader(String key, Collection<String> value) {
        if(this.header == null){
            this.header = new LinkedHashMap<>();
        }
        this.header.put(key, value);
    }


    public static Builder builder(){
        return new Builder(new WebSocketRequest());
    }

    public static WebSocketRequest getRequestToWebSocketRequest(RequestTemplate requestTemplate){
        byte[] byteId = new byte[32];
        ThreadLocalRandom.current().nextBytes(byteId);
        return WebSocketRequest.builder()
                .requestId(requestTemplate.getRequestPath())
                .body(requestTemplate.getBodyTemplate().getBody())
                .header(requestTemplate.getHeaders())
                .replyTo(String.format("socket.invoker.rpc.reply.%s", UUID.randomUUID()))
                .build();
    }

    public static class Builder{
        private WebSocketRequest request;

        public Builder(WebSocketRequest request) {
            this.request = request;
        }
        public Builder body(byte[] bytes){
            request.setBody(bytes);
            return this;
        }

        public Builder requestId(String requestId){
            request.setRequestId(requestId);
            return this;
        }

        public Builder replyTo(String replyTo){
            request.setReplyTo(replyTo);
            return this;
        }

        public void addHeader(String key, Collection<String> value){
            request.addHeader(key, value);
        }

        public Builder header(Map<String, Collection<String>> header) {
            request.setHeaders(header);
            return this;
        }

        public WebSocketRequest build(){
            return request;
        }
    }
}
