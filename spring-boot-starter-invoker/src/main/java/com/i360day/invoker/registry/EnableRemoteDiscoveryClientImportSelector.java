package com.i360day.invoker.registry;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * invoker auto  configuration importSelector
 *
 * @Author liju.z
 * @Date 2025/4/14 22:42
 */
public class EnableRemoteDiscoveryClientImportSelector implements ImportSelector {
    private final String REMOTE_CLIENT_POST_PROCESSOR = "com.i360day.invoker.registry.RemoteClientBeanDefinitionRegistryPostProcessor";

    /**
     * 指定类自动注册上下文
     *
     * @param importingClassMetadata
     * @return
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{ RemoteClientBeanDefinitionRegistryPostProcessor.class.getName() };
    }
}