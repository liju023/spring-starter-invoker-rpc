package com.i360day.invoker.context;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * TODO 后期做子容器隔离
 *
 * @author liju.z
 */
public interface InvokerContext extends DisposableBean {

    ApplicationContext getParent();

    <T> Map<String, T> getBeanOfMap(Class<T> type);

    <T> T getBean(String contextName, String beanName, Class<T> type);

    <T> T getBean(String beanName, Class<T> type);

    <T> T getBean(String beanName);

    <T> T getBean(Class<T> type);

    <T> T getBean(Class<T> type, boolean ignoreError);

    <T> T createBean(Class<T> beanClass);

    /**
     * 注入任意对象中所需要的spring上下文中的bean
     *
     * @param beanObject
     * @return
     */
    default <T> T injectBeanObject(T beanObject) {
        AutowiredAnnotationBeanPostProcessor postProcessor = getBean(AutowiredAnnotationBeanPostProcessor.class);
        postProcessor.processInjection(beanObject);
        return beanObject;
    }
}
