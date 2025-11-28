package com.i360day.invoker.common;

import com.i360day.invoker.annotation.RemoteModule;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具
 *
 * @author: liju.z
 * @Date: 2022-08-13 16:03
 **/
public class RemoteModuleUtils {

    /**
     * 获取remoteModule
     * @param interfaceClazz
     * @return
     */
    public static RemoteModule getRemoteModule(Class<?> interfaceClazz) {
        //获取父类配置
        Class<?>[] interfaceClazzSuper = interfaceClazz.getInterfaces();
        //获取到RemoteModule集合
        List<RemoteModule> remoteModuleList = Arrays.asList(interfaceClazzSuper)
                .stream()
                .map(clazz -> AnnotatedElementUtils.getMergedAnnotation(clazz, RemoteModule.class))
                .filter(remoteModule -> remoteModule != null)
                .collect(Collectors.toList());
        //判断service暴露类，如果有remoteModule，那么只能有一个remoteModule
        if (!remoteModuleList.isEmpty()) {
            Assert.isTrue(remoteModuleList.size() == 1, String.format("%s Can't inherit more than one module interface", interfaceClazz.getName()));
            return remoteModuleList.stream().findFirst().get();
        }
        //log提示，接口类中未出现RemoteModule标记
        return AnnotatedElementUtils.findMergedAnnotation(interfaceClazz, RemoteModule.class);
    }

    /**
     * 获取remoteModule map
     * @param interfaceClazz
     * @return
     */
    public static Map<String, Object> getRemoteModuleToMap(Class<?> interfaceClazz){
        //父类
        RemoteModule remoteModule = getRemoteModule(interfaceClazz);
        Map<String, Object> parentAnnotationAttributes = AnnotationUtils.getAnnotationAttributes(remoteModule, true);

        //当前类
        RemoteModule mergedAnnotation = AnnotatedElementUtils.findMergedAnnotation(interfaceClazz, RemoteModule.class);
        Map<String, Object> currentAnnotationAttributes = AnnotationUtils.getAnnotationAttributes(mergedAnnotation, true);

        //合并
        parentAnnotationAttributes.forEach((key, value) -> {
            if (ObjectUtils.isNotEmpty(value) && ObjectUtils.isEmpty(currentAnnotationAttributes.get(key))) {
                currentAnnotationAttributes.put(key, value);
            }
        });

        return currentAnnotationAttributes;
    }
}
