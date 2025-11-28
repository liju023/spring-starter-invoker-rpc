package com.i360day.invoker.registry;

import com.i360day.invoker.annotation.RemoteClientEntity;
import com.i360day.invoker.common.*;
import com.i360day.invoker.hystrix.DefaultFallbackFactory;
import com.i360day.invoker.hystrix.FallbackFactory;
import com.i360day.invoker.support.BasicAbstractPropertyResolver;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <p> @description:     <p>
 * <p>
 * <p> @author: 胡.青牛   <p>
 * <p>
 * <p> @date: 2019/4/30 0030  13:04
 **/
abstract class AbstractRemoteScanAnnotationParser extends BasicAbstractPropertyResolver implements ResourceLoaderAware {
    private ResourceLoader resourceLoader;
    private final static Map<Class, Object> cacheBeanRegisters = new ConcurrentHashMap();

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Map<Class, Object> getCacheBeanRegisters() {
        return cacheBeanRegisters;
    }

    /**
     * 注册bean，并添加到缓存
     *
     * @param registry
     * @param beanDefinitionHolder
     * @return
     */
    public BeanDefinitionHolder registerBean(Class clazz, BeanDefinitionRegistry registry, BeanDefinitionHolder beanDefinitionHolder) {
        if (beanDefinitionHolder == null || registry == null) return beanDefinitionHolder;

        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
        if (cacheBeanRegisters.get(clazz) != null) {
            logger.error(String.format("repeat register [%s] There is already a proxy bean object in the context, please check the @RemoteScan basePackages configuration", clazz));
        }
        cacheBeanRegisters.put(clazz, beanDefinitionHolder);
        return beanDefinitionHolder;
    }

    /**
     * 扫描包下的class
     *
     * @return
     */
    protected final ClassPathScanningCandidateComponentProvider getScanner() {
        return getScanner((beanDefinition) -> beanDefinition.getMetadata().isIndependent() && beanDefinition.getMetadata().isInterface());
    }

    /**
     * 扫描包下的class
     *
     * @return
     */
    protected final ClassPathScanningCandidateComponentProvider getScanner(Function<AnnotatedBeanDefinition, Boolean> function) {
        return new ClassPathScanningCandidateComponentProvider(false, getEnvironment()) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return function.apply(beanDefinition);
            }
        };
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 获取包名 <p>
     *
     * <p> @Date  9:07 <p>
     *
     * <p> @Param [importingClassMetadata] <p>
     *
     * <p> @return [importingClassMetadata] <p>
     **/
    protected String[] getPackageName(AnnotationMetadata importingClassMetadata) {
        if (importingClassMetadata instanceof StandardAnnotationMetadata) {
            Package packages = ((StandardAnnotationMetadata) importingClassMetadata).getIntrospectedClass().getPackage();
            return packages == null ? null : new String[]{packages.getName()};
        }
        return new String[]{ClassUtils.getClass(importingClassMetadata.getClassName()).getPackageName()};
    }

    /**
     * <p> @Description: 制定bean创建规则   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/11 0011 16:36   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    protected String getBeanName(Class<?> clazz) {
        return clazz.getName() + "." + InvokerBeanNameSpecification.class.getSimpleName();
    }

    /**
     * 解析客户端信息
     *
     * @param attributes
     * @return
     */
    protected final RemoteClientEntity parseRemoteClient(Map<String, Object> attributes) {
        RemoteClientEntity entity = new RemoteClientEntity();

        //账号
        String username = parsePlaceHolder(ObjectUtils.toString(attributes.get("clientUser")), () -> {
            return parsePlaceHolder("${spring.invoker.global-client.client-user}", () -> parsePlaceHolder("${spring.invoker.globalClient.clientUser:invoker}"));
        });
        entity.setClientUser(username);

        //密码
        String password = parsePlaceHolder(ObjectUtils.toString(attributes.get("clientPassword")), () -> {
            return parsePlaceHolder("${spring.invoker.global-client.client-password}", () -> parsePlaceHolder("${spring.invoker.globalClient.clientPassword:invoker}"));
        });
        entity.setClientPassword(password);

        //分组
        String group = ObjectUtils.toString(attributes.get("group"));
        if (ObjectUtils.isEmpty(group)) {
            group = "";
        } else if (group.startsWith("/")) {
            group = group.substring(1);
        }
        entity.setGroup(group);

        //版本
        String version = ObjectUtils.toString(attributes.get("version"));
        if (ObjectUtils.isEmpty(version)) {
            version = "";
        } else if (version.startsWith("/")) {
            version = version.substring(1);
        }
        entity.setVersion(version);

        //remote client address
        String[] address = Stream.of((String[]) attributes.get("address")).map(clientUrl -> parsePlaceHolder(clientUrl)).filter(f -> f != null).toArray(String[]::new);
        if (ObjectUtils.isEmpty(address)) {
            address = new String[]{"invoker://unknown"};
        }
        entity.setAddress(address);

        //回调
        Class<?> fallbackClazz = ObjectUtils.toClass(attributes.get("fallback"));
        String fallbackClassName = parsePlaceHolder("${spring.invoker.global-client.fallback}", () -> parsePlaceHolder("${spring.invoker.globalClient.fallback}"));
        if ((null == fallbackClazz || void.class.equals(fallbackClazz)) && fallbackClassName != null) {
            fallbackClazz = ClassUtils.getClass(fallbackClassName);
        }
        entity.setFallback(fallbackClazz);

        //回调工厂
        Class<?> fallbackFactoryObj = ObjectUtils.toClass(attributes.get("fallbackFactory"));
        Assert.isTrue(ClassUtils.isClassContainInterface(fallbackFactoryObj, FallbackFactory.class), String.format("[%s] Not an implementation class of [%s]", fallbackFactoryObj, FallbackFactory.class));

        //识别回调工厂
        Class<? extends FallbackFactory> fallbackFactory = (Class<? extends FallbackFactory>) fallbackFactoryObj;
        String fallbackFactoryClassName = parsePlaceHolder("${spring.invoker.global-client.fallback-factory}", () -> parsePlaceHolder("${spring.invoker.globalClient.fallbackFactory}"));
        if ((null == fallbackFactory || DefaultFallbackFactory.class.equals(fallbackFactory)) && fallbackFactoryClassName != null) {
            Assert.isTrue(ClassUtils.isClassContainInterface(ClassUtils.getClass(fallbackFactoryClassName), FallbackFactory.class), String.format("[%s] Not an implementation class of [%s]", fallbackFactoryClassName, FallbackFactory.class));
            fallbackFactory = (Class<? extends FallbackFactory>) ClassUtils.getClass(fallbackFactoryClassName);
        }
        entity.setFallbackFactory(fallbackFactory);
        return entity;
    }

    /**
     * 注册remote bean
     *
     * @param registry
     * @param interfaceClazz
     * @param attributes
     * @param clientProxyClass
     * @param serverProxyClass
     */
    protected final void registerRemoteBean(BeanDefinitionRegistry registry, Class<?> interfaceClazz, Map<String, Object> attributes, Class<?> clientProxyClass, Class<?> serverProxyClass) {
        //判断当前工程中，该接口是否已在spring上下文中存在，并且class路径未包含当前接口的实现
        if (!BeanDefinitionRegistryUtils.containsBeanName(registry, interfaceClazz)) {
            registerInterfaceBean(registry, interfaceClazz, attributes, clientProxyClass);
        }
        //通过接口获取到service实现类，注册到beanMappingHandler上。并且当前bean不是HttpInvokerClientFactoryBean代理的class
        else if (!clientProxyClass.equals(BeanDefinitionRegistryUtils.getBeanClass(registry, interfaceClazz, true)) && serverProxyClass != null) {
            //获取注册的server
            registerServerBean(registry, serverProxyClass, attributes, interfaceClazz);
        } else {
            //ignore
            logger.warn(String.format("Unable to recognize the current RPC client %s", interfaceClazz));
        }
    }

    /**
     * 获取默认server BeanDefinition
     * 默认注入service、remoteClientEntity、调用执行处理器、调用跟踪器
     *
     * @param registry       spring bean注册器
     * @param targerClass    目标class
     * @param interfaceClazz 目标class的接口
     * @param attributes     目标class接口的标记属性
     * @return
     */
    protected final BeanDefinitionBuilder getDefaultServerBeanDefinition(BeanDefinitionRegistry registry, Class<?> targerClass, Class<?> interfaceClazz, Map<String, Object> attributes) {
        RemoteClientEntity remoteClientEntity = parseRemoteClient(attributes);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(targerClass);

        //构造函数默认注入RemoteClient相关配置信息
        if (ClassUtils.isConstructorContainsClass(targerClass, RemoteClientEntity.class)) {
            AbstractBeanDefinition rawBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
            int[] constructorClassIndex = ClassUtils.getConstructorClassIndex(targerClass, RemoteClientEntity.class);
            for (int classIndex : constructorClassIndex) {
                rawBeanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(classIndex, remoteClientEntity);
            }
        }

        //必要属性
        beanDefinitionBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        try {
            //该地方获取@SpringBootApplication上下文中的service，获取失败后InitializingBean会再次自动获取。有可能在执行注册的时候service还未注册到上下文中
            beanDefinitionBuilder.addPropertyReference("service", BeanDefinitionRegistryUtils.getBeanName0(registry, interfaceClazz));
        } catch (Exception ex) {
            //ignore ex
        }
        beanDefinitionBuilder.addPropertyValue("serviceInterface", interfaceClazz);
        beanDefinitionBuilder.addPropertyValue("encoderClass", attributes.get("encoder"));
        beanDefinitionBuilder.addPropertyReference("remoteInvocationExecutor", InvokerConstant.REMOTE_INVOCATION_EXECUTOR);
        beanDefinitionBuilder.addPropertyReference("remoteInvocationTraceInterceptor", InvokerConstant.REMOTE_INVOCATION_TRACE_INTERCEPTOR);
        return beanDefinitionBuilder;
    }

    /**
     * 注册interface bean
     *
     * @param registry
     * @param interfaceClazz
     * @param attributes
     * @param clientProxyClass
     */
    protected void registerInterfaceBean(BeanDefinitionRegistry registry, Class<?> interfaceClazz, Map<String, Object> attributes, Class<?> clientProxyClass) {
        RemoteClientEntity remoteClientEntity = parseRemoteClient(attributes);
        //获取服务端地址
        URI[] serverUrl = remoteClientEntity.getClientUrl(interfaceClazz);
        String clazzName = getBeanName(interfaceClazz);

        //已经存在了则不添加了
        if (!BeanDefinitionRegistryUtils.containsBeanName(registry, clazzName)) {

            //创建代理bean
            BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(clientProxyClass);
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

            //构造函数默认注入RemoteClient相关配置信息
            if (ClassUtils.isConstructorContainsClass(clientProxyClass, RemoteClientEntity.class)) {
                AbstractBeanDefinition rawBeanDefinition = definition.getRawBeanDefinition();
                int[] constructorClassIndex = ClassUtils.getConstructorClassIndex(clientProxyClass, RemoteClientEntity.class);
                for (int classIndex : constructorClassIndex) {
                    rawBeanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(classIndex, remoteClientEntity);
                }
            }

            definition.addPropertyValue("serviceUrl", serverUrl);
            definition.addPropertyValue("decoderClass", attributes.get("decoder"));
            definition.addPropertyValue("serviceInterface", interfaceClazz);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            //attribute
            AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
            // has a default, won't be null
            beanDefinition.setPrimary(ObjectUtils.toBoolean(attributes.get("primary")));
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, clazzName, new String[]{clazzName});
            registerBean(interfaceClazz, registry, beanDefinitionHolder);
        }
    }

    /**
     * 注册server bean
     *
     * @param registry
     * @param interfaceClazz
     * @param attributes
     * @param serverProxyClass
     * @return
     */
    protected void registerServerBean(BeanDefinitionRegistry registry, Class<?> serverProxyClass, Map<String, Object> attributes, Class<?> interfaceClazz) {
        RemoteClientEntity remoteClientEntity = parseRemoteClient(attributes);
        //url
        URI serverUrl = remoteClientEntity.getServiceUrl(interfaceClazz);
        //已注册过，则忽略
        if (!BeanDefinitionRegistryUtils.containsBeanName(registry, serverUrl.getPath())) {
            BeanDefinitionBuilder beanDefinitionBuilder = getDefaultServerBeanDefinition(registry, serverProxyClass, interfaceClazz, attributes);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinitionBuilder.getBeanDefinition(), serverUrl.getPath(), new String[]{interfaceClazz.getName()});
            registerBean(interfaceClazz, registry, beanDefinitionHolder);
        }
    }
}
