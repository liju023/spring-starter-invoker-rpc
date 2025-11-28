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
package com.i360day.invoker.registry;

import com.i360day.invoker.annotation.RemoteClient;
import com.i360day.invoker.annotation.RemoteScan;
import com.i360day.invoker.exception.RemoteScanBeanDefinitionRegistryException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

/**
 * 初始化自定义扫描
 *
 * @author liju.z
 * @date 2023/5/12 22:38
 */
class RemoteClientBeanDefinitionRegistryPostProcessor extends RemoteClientImportBeanDefinitionRegistrar<RemoteClient> implements BeanDefinitionRegistryPostProcessor {

    /**
     * 元数据读取器
     */
    private MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    /**
     * get annotation metadata
     *
     * @param beanClassName
     * @return
     */
    protected AnnotationMetadata getAnnotationMetadata(String beanClassName) {
        try {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(beanClassName);
            return metadataReader.getAnnotationMetadata();
        } catch (Exception ex) {
            //ignore ex
            return null;
        }
    }

    /**
     * 使用后置 注册RemoteClient
     *
     * @param registry the bean definition registry used by the application context
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            RegisteredBean registeredBean = RegisteredBean.of((ConfigurableListableBeanFactory) registry, beanDefinitionName);
            try {
                AnnotationMetadata annotationMetadata = getAnnotationMetadata(registeredBean.getBeanClass().getName());
                //找到自定义@RemoteScan扫描路径
                if (annotationMetadata != null && annotationMetadata.getAnnotations().isPresent(RemoteScan.class.getName())) {
                    registerBeanDefinitions(annotationMetadata, registry);
                }
            } catch (Exception e) {
                throw new RemoteScanBeanDefinitionRegistryException(e);
            }
        }
    }
}
