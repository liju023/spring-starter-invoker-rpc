package com.i360day.invoker.registry;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p> @description:     <p>
 * <p>
 * <p> @author: 胡.青牛   <p>
 * <p>
 * <p> @date: 2019/5/11 0011  15:45
 **/
interface BeanDefinitionRegistrarFactory {

    /**
     * register Bean
     *
     * @param importingClassMetadata
     * @param registry
     */
    void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);
}
