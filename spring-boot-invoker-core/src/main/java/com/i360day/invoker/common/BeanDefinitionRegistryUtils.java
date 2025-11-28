package com.i360day.invoker.common;

import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @description:
 * @author: 胡.青牛
 * @date: 2019/4/30 0030  12:18
 **/
public class BeanDefinitionRegistryUtils {

    private static boolean checkInstanceofDefaultListableBeanFactory(BeanDefinitionRegistry beanDefinitionRegistry) {
        return beanDefinitionRegistry instanceof DefaultListableBeanFactory;
    }

    /**
     * @Description: 根据class获取bean实体
     * @author: 胡.青牛
     * @Date: 2019/4/30 0030 16:44
     * @return:
     **/
    public static <T> T getBean(BeanDefinitionRegistry beanDefinitionRegistry, Class<T> clazz) {
        if (!checkInstanceofDefaultListableBeanFactory(beanDefinitionRegistry)) {
            return null;
        }

        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;

        return defaultListableBeanFactory.getBean(clazz);
    }

    /**
     * @Description: 根据class获取bean实体
     * @author: 胡.青牛
     * @Date: 2019/4/30 0030 16:44
     * @return:
     **/
    @SuppressWarnings("unchecked")
    public static <T> T getBean(BeanDefinitionRegistry beanDefinitionRegistry, String beanName) {
        if (!checkInstanceofDefaultListableBeanFactory(beanDefinitionRegistry)) {
            return null;
        }

        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;

        return (T) defaultListableBeanFactory.getBean(beanName);
    }

    /**
     * 获取bean类型
     * @param beanDefinitionRegistry
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> Map<String, T> getBeansOfType(BeanDefinitionRegistry beanDefinitionRegistry, Class<T> clazz){
        if (!checkInstanceofDefaultListableBeanFactory(beanDefinitionRegistry)) {
            return null;
        }

        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;

        return defaultListableBeanFactory.getBeansOfType(clazz);
    }

    /**
     * 获取 beanDefinition
     *
     * @param beanDefinitionRegistry
     * @param beanName
     * @return
     */
    public static BeanDefinition getBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, String beanName) {
        return beanDefinitionRegistry.getBeanDefinition(beanName);
    }

    /**
     * 获取 beanDefinition
     *
     * @param beanDefinitionRegistry
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> BeanDefinition getBeanDefinition(BeanDefinitionRegistry beanDefinitionRegistry, Class<T> clazz) {
        if (!checkInstanceofDefaultListableBeanFactory(beanDefinitionRegistry)) {
            return null;
        }
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;

        String[] beanNamesForType = defaultListableBeanFactory.getBeanNamesForType(clazz, true, false);

        if (beanNamesForType == null || beanNamesForType.length == 0) throw new NoUniqueBeanDefinitionException(clazz);
        //多个实现
        if (beanNamesForType.length > 1) {
            //多个处理逻辑
            Optional<BeanDefinition> beanDefinitionOptional = Arrays.stream(beanNamesForType)
                    .map(m -> defaultListableBeanFactory.getBeanDefinition(m))
                    .filter(f -> f.isPrimary())
                    .findFirst();
            return beanDefinitionOptional.orElseThrow(() -> new NoUniqueBeanDefinitionException(clazz, beanNamesForType));
        }
        return defaultListableBeanFactory.getBeanDefinition(beanNamesForType[0]);
    }

    /**
     * @Description: 根据class获取beanName
     * @author: 胡.青牛
     * @Date: 2019/4/30 0030 16:44
     * @return:
     **/
    public static String[] getBeanNames(BeanDefinitionRegistry beanDefinitionRegistry, Class<?> clazz) {
        if (!checkInstanceofDefaultListableBeanFactory(beanDefinitionRegistry)) {
            return new String[]{};
        }
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;
        return defaultListableBeanFactory.getBeanNamesForType(clazz, true, false);
    }

    /**
     * 获取一个bean name
     * @param beanDefinitionRegistry
     * @param clazz
     * @return
     */
    public static String getBeanName0(BeanDefinitionRegistry beanDefinitionRegistry, Class<?> clazz) {
        if (!checkInstanceofDefaultListableBeanFactory(beanDefinitionRegistry)) {
            return null;
        }
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;
        String[] beanNamesForType = defaultListableBeanFactory.getBeanNamesForType(clazz, true, false);
        Assert.isTrue(beanNamesForType.length > 0, String.format("%s not found bean", clazz));

        //多个实现
        if (beanNamesForType.length > 1) {
            //多个处理逻辑
            Optional<String> beanNameOptional = Arrays.stream(beanNamesForType).filter(f -> defaultListableBeanFactory.getBeanDefinition(f).isPrimary()).findFirst();
            if (beanNameOptional.isPresent()) {
                return beanNameOptional.get();
            }
            throw new NoUniqueBeanDefinitionException(clazz, beanNamesForType);
        }
        return beanNamesForType[0];
    }

    /**
     * @Description: 获取bean是否存在
     * @author: 胡.青牛
     * @Date: 2019/4/30 0030 16:44
     * @return:
     **/
    public static boolean containsBeanName(BeanDefinitionRegistry beanDefinitionRegistry, Class<?> clazz) {
        String[] beanNames = getBeanNames(beanDefinitionRegistry, clazz);
        return beanNames != null && beanNames.length > 0;
    }

    /**
     * @Description: 获取bean是否存在
     * @author: 胡.青牛
     * @Date: 2019/4/30 0030 16:44
     * @return:
     **/
    public static boolean containsBeanName(BeanDefinitionRegistry beanDefinitionRegistry, String clazz) {
        return Arrays.asList(beanDefinitionRegistry.getBeanDefinitionNames()).contains(clazz);
    }

    /**
     * 创建 BeanDefinition
     *
     * @param clazz
     * @return
     */
    public static BeanDefinition createBean(Class<?> clazz) {
        return new RootBeanDefinition(clazz);
    }

    /**
     * 获取bean class
     *
     * @param definitionRegistry
     * @param clazz
     * @param ignoreEx
     * @return
     */
    public static Class<?> getBeanClass(BeanDefinitionRegistry definitionRegistry, Class<?> clazz, boolean ignoreEx) {
        try{
            return getBeanClass(definitionRegistry, clazz);
        }catch (Exception ex){
            if (ignoreEx){
                return null;
            }
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 获取bean class
     *
     * @param definitionRegistry
     * @param clazz
     * @return
     */
    public static Class<?> getBeanClass(BeanDefinitionRegistry definitionRegistry, Class<?> clazz) {
        try{
            String beanName = getBeanName0(definitionRegistry, clazz);
            RegisteredBean registeredBean = RegisteredBean.of((ConfigurableListableBeanFactory) definitionRegistry, beanName);
            return registeredBean.getBeanClass();
        }catch (Exception ex){
            BeanDefinition beanDefinition = getBeanDefinition(definitionRegistry, clazz);
            if(ObjectUtils.isNotEmpty(beanDefinition.getBeanClassName())){
                return ClassUtils.getClass(beanDefinition.getBeanClassName());
            }
            return clazz;
        }
    }

    /**
     * 获取bean class
     *
     * @param definitionRegistry
     * @param clazz
     * @return
     */
    public static List<? extends Class<?>> getBeanClassList(BeanDefinitionRegistry definitionRegistry, Class<?> clazz) {
        Map<String, ?> beansOfType = getBeansOfType(definitionRegistry, clazz);
        return beansOfType.keySet().stream().map(beanName -> {
            RegisteredBean registeredBean = RegisteredBean.of((ConfigurableListableBeanFactory) definitionRegistry, beanName);
            return registeredBean.getBeanClass();
        }).toList();
    }
}
