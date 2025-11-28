package com.i360day.invoker.registry;

import com.i360day.invoker.annotation.RemoteModule;
import com.i360day.invoker.annotation.RemoteScan;
import com.i360day.invoker.annotation.RemoteService;
import com.i360day.invoker.common.ClassUtils;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.common.RemoteModuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 可重写的 BeanDefinitionRegistrar
 */
class RemoteClientImportBeanDefinitionRegistrar<T extends Annotation> extends AbstractRemoteScanAnnotationParser implements BeanDefinitionRegistrarFactory {

    private Logger logger = LoggerFactory.getLogger(RemoteClientImportBeanDefinitionRegistrar.class);

    private Class<? extends Annotation> annotationClass;

    public RemoteClientImportBeanDefinitionRegistrar() {
        this.annotationClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(this.getClass(), RemoteClientImportBeanDefinitionRegistrar.class);
    }

    /**
     * <p> @Description: 扫描包   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/11 0011 16:45   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取扫描包
        Map<String, Object> remoteScanAttrMap = importingClassMetadata.getAnnotationAttributes(RemoteScan.class.getName(), true);

        //拿到扫描包路径
        Object basePackages = remoteScanAttrMap.get("basePackages");
        if (ObjectUtils.isPackage(basePackages) && logger.isDebugEnabled()) {
            logger.debug("@RemoteScan attribute (basePackages/value) is null，@RemoteScan/@EnableRemoteDiscoveryClient attribute basePackages conflict error.");
        }
        String[] packages = ObjectUtils.isPackage(basePackages) ? getPackageName(importingClassMetadata) : (String[]) basePackages;
        Assert.isTrue(!ObjectUtils.isPackage(packages), "@RemoteScan attribute (basePackages/value) is required");

        //找到包下的class并注册bean
        findPackageClassAndRegistryBean(registry, packages);
    }

    /**
     * 获取@RemoteService上的接口列表
     *
     * @param packages
     * @return
     */
    private List<? extends Class<?>> getRemoteServiceToInterfaceList(String... packages) {
        //扫描class中@RemoteClient标记
        ClassPathScanningCandidateComponentProvider scanner = getScanner(beanDefinition -> beanDefinition.isSingleton() && !beanDefinition.getMetadata().isInterface());
        scanner.setResourceLoader(getResourceLoader());
        scanner.addIncludeFilter(new AnnotationTypeFilter(RemoteService.class));

        //多个包路径
        return Arrays.asList(packages).stream().flatMap(aPackage -> {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(aPackage);

            return beanDefinitions.stream()
                    .filter(beanDefinition -> beanDefinition instanceof AnnotatedBeanDefinition)
                    .flatMap(beanDefinition -> {
                        try {
                            String className = ((AnnotatedBeanDefinition) beanDefinition).getMetadata().getClassName();
                            Class<?> serviceClass = ClassUtils.getClass(className);
                            return serviceClass != null ? Stream.of(serviceClass.getInterfaces()) : null;
                        } catch (Exception ex) {
                            return null;
                        }
                    }).filter(f -> f != null);
        }).collect(Collectors.toList());
    }

    /**
     * <p> @Description: 获取指定包下的class，分析注册bean   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/11 0011 16:45   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    protected void findPackageClassAndRegistryBean(BeanDefinitionRegistry registry, String... packages) {
        //扫描class中@RemoteClient标记
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(getResourceLoader());
        scanner.addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));

        //多个包路径
        for (String aPackage : packages) {

            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(aPackage);

            //
            beanDefinitions.stream()
                    .filter(beanDefinition -> beanDefinition instanceof AnnotatedBeanDefinition)
                    .forEach(beanDefinition -> {
                        registerRemoteClientBean(registry, ((AnnotatedBeanDefinition) beanDefinition).getMetadata());
                    });
        }
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 获取到RemoteService，将相应的bean进行创建 <p>
     *
     * <p> @Date  2022-08-13 15:00 <p>
     *
     * <p> @Param registry <p>
     * <p> @Param beanDefinition <p>
     **/
    protected void registerRemoteClientBean(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata) {
        // verify annotated class is an interface
        Assert.isTrue(annotationMetadata.isInterface(), "@RemoteClient can only be specified on an interface");

        //get annotation attributes
        Map<String, Object> remoteClientAttributes = annotationMetadata.getAnnotationAttributes(annotationClass.getName());
        Class<?> clientProxyClass = ClassUtils.getTargetClass(remoteClientAttributes.get("clientProxyClass"));
        Class<?> serverProxyClass = ClassUtils.getTargetClass(remoteClientAttributes.get("serverProxyClass"));
        Class<?> interfaceClazz = ClassUtils.getClass(annotationMetadata.getClassName());

        //获取父类配置，始终优先读取父类
        RemoteModule remoteModule = RemoteModuleUtils.getRemoteModule(interfaceClazz);
        Assert.notNull(remoteModule, String.format("%s Not included RemoteModule/RemoteClient annotation", interfaceClazz));
        Map<String, Object> remoteModuleAttributes = AnnotationUtils.getAnnotationAttributes(remoteModule, true);
        remoteModuleAttributes.forEach((key, value) -> {
            if (ObjectUtils.isNotEmpty(value) && ObjectUtils.isEmpty(remoteClientAttributes.get(key))) {
                remoteClientAttributes.put(key, value);
            }
        });

        //验证客户端规则
        validateRemoteClient(annotationMetadata, remoteClientAttributes, interfaceClazz);

        //注册bean
        super.registerRemoteBean(registry, interfaceClazz, remoteClientAttributes, clientProxyClass, serverProxyClass);
    }

    /**
     * 验证客户端
     * @param annotationMetadata
     * @param interfaceClazz
     */
    protected void validateRemoteClient(AnnotationMetadata annotationMetadata, Map<String, Object> remoteClientAttributes, Class<?> interfaceClazz){

        //verify address
        Stream.of((String[]) remoteClientAttributes.get("address")).forEach(oldAddress -> {
            String address = parsePlaceHolder(oldAddress, oldAddress);

            //为空不处理
            if (ObjectUtils.isEmpty(address)) {
                return;
            }
            //check @RemoteRedisClient address
            if (null != annotationMetadata.getAnnotationAttributes("com.i360day.invoker.annotation.RemoteRedisClient")) {
                Assert.isTrue(address.startsWith("redis://"),
                        String.format("%s @RemoteRedisClient address [%s] error,  must start with 'redis://', as redis://x.x.x.x:6379", interfaceClazz.getName(), address)
                );
            }
            //check @RemoteWebSocketClient address
            else if (null != annotationMetadata.getAnnotationAttributes("com.i360day.invoker.annotation.RemoteWebSocketClient")) {
                Assert.isTrue(address.startsWith("ws://") || address.startsWith("wss://"),
                        String.format("%s @RemoteWebSocketClient address [%s] error,  must start with ['ws://', 'wss://'], as ws://x.x.x.x:8080", interfaceClazz.getName(), address)
                );
            }
            //check @RemoteAmqpClient address
            else if (null != annotationMetadata.getAnnotationAttributes("com.i360day.invoker.annotation.RemoteAmqpClient")) {
                Assert.isTrue(address.startsWith("amqp://"),
                        String.format("%s @RemoteAmqpClient address [%s] error,  must start with 'amqp://', as amqp://x.x.x.x:5762", interfaceClazz.getName(), address)
                );
            }
            //check @RemoteCloudClient address
            else if (null != annotationMetadata.getAnnotationAttributes("com.i360day.invoker.annotation.RemoteCloudClient")) {
                Assert.isTrue(address.startsWith("http://") || address.startsWith("https://"),
                        String.format("%s @RemoteCloudClient address [%s] error,  must start with ['http://'、'https://'], as http://x.x.x.x:8080", interfaceClazz.getName(), address)
                );
            }
            //check @RemoteClient address
            else if (null != annotationMetadata.getAnnotationAttributes("com.i360day.invoker.annotation.RemoteClient")) {
                Assert.isTrue(address.startsWith("http://") || address.startsWith("https://"),
                        String.format("%s @RemoteClient address [%s] error,  must start with ['http://'、'https://'], as http://x.x.x.x:8080", interfaceClazz.getName(), address)
                );
            }
        });
    }
}
