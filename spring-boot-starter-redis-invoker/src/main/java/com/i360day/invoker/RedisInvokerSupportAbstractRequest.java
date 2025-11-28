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

import com.i360day.invoker.common.InvokerUrlUtils;
import com.i360day.invoker.proxy.TargetProxy;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liju.z
 * @date 2025/6/8 11:14
 */
public abstract class RedisInvokerSupportAbstractRequest implements RedisReplyToMessageListener{
    private final Map<String, RedisConnectionFactory> multiTenancyConnectionFactory = new LinkedHashMap<>();
    private final RedisConnectionFactory defaultConnectionFactory;

    /**
     * create redis request
     *
     * @param redisConnectionFactory
     */
    public RedisInvokerSupportAbstractRequest(RedisConnectionFactory redisConnectionFactory) {
        this.defaultConnectionFactory = redisConnectionFactory;

        //default context redisConnectionFactory
        this.multiTenancyConnectionFactory.put("unknown", this.defaultConnectionFactory);
    }

    /**
     * 获取连接
     * @param requestTemplate
     * @return
     */
    protected RedisConnectionFactory getRedisConnectionFactory(RequestTemplate requestTemplate){
        String redisConnectionFactoryKey = requestTemplate.getUri().getAuthority();

        RedisConnectionFactory redisConnectionFactory = this.multiTenancyConnectionFactory.get(redisConnectionFactoryKey);

        //根据RemoteRedisClient中的指定redis创建连接工厂
        if (null == redisConnectionFactory) {

            List<InvokerUrlUtils.NetworkAddress> redisConfigList = InvokerUrlUtils.resolveNetworkAddressList(requestTemplate.getUri().toString(), ";");
            redisConnectionFactoryKey = redisConfigList.stream().map(m -> String.format("%s:%s", m.getHost(), m.getPort())).collect(Collectors.joining("-"));

            redisConnectionFactory = multiTenancyConnectionFactory.get(redisConnectionFactoryKey);
            if (redisConnectionFactory == null) {
                TargetProxy targetProxy = requestTemplate.getTargetProxy();
                synchronized (multiTenancyConnectionFactory) {
                    redisConnectionFactory = Optional.ofNullable(multiTenancyConnectionFactory.get(redisConnectionFactoryKey)).orElseGet(() -> {
                        //如果地址为多个则集群方式
                        if (redisConfigList.size() > 1) {
                            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(redisConfigList.stream().map(m -> m.getHost()).collect(Collectors.toList()));
//                            redisClusterConfiguration.setUsername(targetProxy.getAnnotationAttributeAsString("username"));
                            redisClusterConfiguration.setPassword(targetProxy.getAnnotationAttributeAsString("password"));
                            //TODO 链接工厂， 该地方只支持 [lettuce-core] 包。后期改造
                            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisClusterConfiguration);
//                            lettuceConnectionFactory.start();
                            lettuceConnectionFactory.afterPropertiesSet();
                            return lettuceConnectionFactory;
                        }
                        //单个
                        else {
                            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisConfigList.get(0).getHost(), redisConfigList.get(0).getPort());
//                            redisStandaloneConfiguration.setUsername(targetProxy.getAnnotationAttributeAsString("username"));
                            redisStandaloneConfiguration.setPassword(targetProxy.getAnnotationAttributeAsString("password"));
                            redisStandaloneConfiguration.setDatabase(Integer.valueOf(targetProxy.getAnnotationAttributeAsString("database")));
                            //TODO 链接工厂， 该地方只支持 [lettuce-core] 包。后期改造
                            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
//                            lettuceConnectionFactory.start();
                            lettuceConnectionFactory.afterPropertiesSet();
                            return lettuceConnectionFactory;
                        }
                    });
                    multiTenancyConnectionFactory.put(redisConnectionFactoryKey, redisConnectionFactory);
                }
            }
        }
        return redisConnectionFactory;
    }
}
