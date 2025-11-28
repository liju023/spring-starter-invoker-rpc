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
package com.i360day.invoker.annotation;

import com.i360day.invoker.common.InvokerUrlUtils;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.stream.Stream;

/**
 * @author liju.z
 * @date 2023/5/12 22:14
 */
public class RemoteClientEntity {
    private Logger logger = LoggerFactory.getLogger(RemoteClientEntity.class);
    /**
     * 接口调用出错后，回调类
     *
     * @return
     */
    private Class<?> fallback;

    /**
     * 回调类工厂
     *
     * @return
     */
    private Class<? extends FallbackFactory> fallbackFactory;

    /**
     * 如果address不等于空，则向这个地址发送HTTP请求
     *
     * @return
     */
    private String[] address;

    /**
     * 如果出现多个同样的bean，设置该bean为默认
     *
     * @return
     */
    private boolean primary;

    /**
     * 分组
     * 如：v1、v2、v3...
     *
     * @return
     */
    private String group;

    /**
     * 版本号
     * 如：1.0.0
     *
     * @return
     */
    private String version;
    /**
     * 账号
     */
    private String clientUser;
    /**
     * 密码
     */
    private String clientPassword;

    public Class<?> getFallback() {
        return fallback;
    }

    public void setFallback(Class<?> fallback) {
        this.fallback = fallback;
    }

    public Class<? extends FallbackFactory> getFallbackFactory() {
        return fallbackFactory;
    }

    public void setFallbackFactory(Class<? extends FallbackFactory> fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
    }

    public String[] getAddress() {
        return address;
    }

    public void setAddress(String[] address) {
        this.address = address;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClientUser() {
        return clientUser;
    }

    public void setClientUser(String clientUser) {
        this.clientUser = clientUser;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    /**
     * 获取server地址
     *
     *
     * @param interfaceClazz
     * @return
     */
    public URI[] getClientUrl(Class<?> interfaceClazz) {
        //ip
        return Stream.of(getAddress()).map(address -> {
            Assert.isTrue(ObjectUtils.isNotEmpty(address) || !"unknown".equals(address), String.format("%s Get [ @RemoteClient or @RemoteModule ] address is empty, please check your configuration !", interfaceClazz));

            //create url
            return InvokerUrlUtils.getClientUrl(address, interfaceClazz, getGroup(), getVersion());
        }).toArray(URI[] ::new);
    }

    /**
     * 获取service url
     * @param interfaceClazz
     * @return
     */
    public URI getServiceUrl(Class<?> interfaceClazz){
        //请求地址
        String serviceUrl = InvokerUrlUtils.getAssembleUrl("http://unknown-service", getGroup(), getVersion());
        //重组url
        return URI.create(InvokerUrlUtils.getServiceUrl(serviceUrl, null, interfaceClazz));
    }
}
