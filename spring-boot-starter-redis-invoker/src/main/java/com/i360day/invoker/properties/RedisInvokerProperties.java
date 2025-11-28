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
package com.i360day.invoker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liju.z
 * @date 2025/6/8 11:35
 */
@ConfigurationProperties(prefix = "spring.invoker.redis" )
public class RedisInvokerProperties {
    /**
     * redis 消息模式
     */
    private ModeType modeType = ModeType.PASSIVE;

    /**
     * 服务端被动模式，启动分布式锁
     * true：在多个服务端模式下不会同时触发监听器
     * false：在多个服务端模式下会同时触发多个监听器
     */
    private boolean enableSubscribeLock = true;

    public ModeType getModeType() {
        return modeType;
    }

    public void setModeType(ModeType modeType) {
        this.modeType = modeType;
    }

    public boolean isEnableSubscribeLock() {
        return enableSubscribeLock;
    }

    public void setEnableSubscribeLock(boolean enableSubscribeLock) {
        this.enableSubscribeLock = enableSubscribeLock;
    }
}
