package com.i360day.invoker.context;

import com.i360day.invoker.configuration.InvokerCloudAutoConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class CloudInvokerContext extends NamedContextFactory implements InvokerContext {
    private ApplicationContext parent;
    private Map<Object, Object> cacheBean = new LinkedHashMap<>();

    public CloudInvokerContext() {
        super(InvokerCloudAutoConfiguration.class, "invoker", "invoker.client.name");
    }

    @Override
    public void setApplicationContext(ApplicationContext parent) throws BeansException {
        super.setApplicationContext(this.parent = parent);
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
        return (T) getContext(contextName).getBean(beanName, type);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> type) {
        return (T) getContext(beanName).getBeansOfType(type);
    }

    @Override
    public <T> T getBean(String beanName) {
        return (T) getContext(beanName);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        return (T) getInstance(type.getSimpleName(), type);
    }

    @Override
    public <T> T getBean(Class<T> type, boolean ignoreError) {
        try {
            return getBean(type);
        } catch (Exception ex) {
            //ignore exception
            if (ignoreError) {
                throw ex;
            }
        }
        return null;
    }

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
