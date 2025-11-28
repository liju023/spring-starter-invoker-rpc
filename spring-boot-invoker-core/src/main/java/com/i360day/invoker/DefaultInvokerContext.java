package com.i360day.invoker;

import com.i360day.invoker.context.InvokerContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultInvokerContext implements ApplicationContextAware, InvokerContext {

    private ApplicationContext parent;

    private Map<Object, Object> cacheBean = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext parent) throws BeansException {
        this.parent = parent;
    }

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public ApplicationContext getParent() {
        return parent;
    }

    @Override
    public <T> Map<String, T> getBeanOfMap(Class<T> type) {
        return parent.getBeansOfType(type, true, false);
    }

    @Override
    public <T> T getBean(String contextName, String beanName, Class<T> type) {
        return getBean(beanName, type);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> type) {
        if (parent.containsBean(beanName)) {
            return (T) parent.getBean(beanName);
        }
        return getBean(type);
    }

    @Override
    public <T> T getBean(String beanName) {
        return (T) parent.getBean(beanName);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        return parent.getBean(type);
    }

    /**
     * 获取bean
     * @param type
     * @param ignoreError
     * @return
     * @param <T>
     */
    @Override
    public <T> T getBean(Class<T> type, boolean ignoreError) {
        try {
            return getBean(type);
        } catch (Exception ex) {
            // ignore
            if (!ignoreError) {
                throw ex;
            }
        }
        return null;
    }

    /**
     * 创建bean
     *
     * @param beanClass
     * @return
     * @param <T>
     */
    @Override
    public <T> T createBean(Class<T> beanClass) {
        Object bean = cacheBean.get(beanClass);
        if(bean == null){
            bean = injectBeanObject(
                    parent.getAutowireCapableBeanFactory().createBean(beanClass)
            );
            cacheBean.put(beanClass, bean);
        }
        return (T) bean;
    }
}
