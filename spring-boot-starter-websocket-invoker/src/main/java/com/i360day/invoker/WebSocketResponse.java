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

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author liju.z
 * @date 2024/3/30 18:09
 */
public class WebSocketResponse implements Serializable {
    private String requestId;
    private String responseId;
    private byte[] body;
    private String replyTo;
    private HttpStatus httpStatus;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
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

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static Buildler builder(){
        return new Buildler(new WebSocketResponse());
    }

    public static class Buildler{
        private WebSocketResponse response;

        public Buildler(WebSocketResponse response) {
            this.response = response;
        }
        public Buildler body(byte[] bytes){
            response.setBody(bytes);
            return this;
        }

        public Buildler requestId(String requestId){
            response.setRequestId(requestId);
            return this;
        }

        public Buildler responseId(String responseId){
            response.setResponseId(responseId);
            return this;
        }

        public Buildler replyTo(String replyTo){
            response.setReplyTo(replyTo);
            return this;
        }

        public Buildler httpStatus(HttpStatus httpStatus){
            response.setHttpStatus(httpStatus);
            return this;
        }

        public WebSocketResponse build(){
            return response;
        }
    }
}
