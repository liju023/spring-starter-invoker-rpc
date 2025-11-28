package com.i360day.invoker.proxy;

import com.i360day.invoker.common.ObjectUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liju.z
 */
public class TargetProxy<T> {
    /**
     * 代理类
     */
    private final Class<T> type;
    /**
     * 代理类名称
     */
    private final String name;
    /**
     * 代理目标url
     */
    private final URI[] url;
    /**
     * 客户端-用户
     */
    private final String clientUser;
    /**
     * 客户端-密码
     */
    private final String clientPassword;
    /**
     * class loader
     */
    private final ClassLoader classLoader;
    /**
     * 代理类的 annotation attributes
     */
    private final Map<Annotation, AnnotationAttributes> annotationAttributesMap;

    /**
     * create TargetProxy
     *
     * @param type
     * @param url
     * @param clientUser
     * @param clientPassword
     * @param classLoader
     */
    public TargetProxy(Class<T> type, URI[] url, String clientUser, String clientPassword, ClassLoader classLoader) {
        this.type = type;
        this.url = url;
        this.clientUser = clientUser;
        this.clientPassword = clientPassword;
        this.classLoader = classLoader;
        this.name = Stream.of(url).map(m -> m.getAuthority()).collect(Collectors.joining(";"));
        this.annotationAttributesMap = new LinkedHashMap<>();

        //resolve Annotation to AnnotationAttributes
        Annotation[] annotations = type.getAnnotations();
        for (Annotation annotation : annotations) {
            AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(this.type, annotation);
            this.annotationAttributesMap.put(annotation, annotationAttributes);
        }
    }

    /**
     * classLoader
     *
     * @return
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 代理名称
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 随机获取一个url
     *
     * @return
     */
    public URI getUrl() {
        int nextInt = ThreadLocalRandom.current().nextInt(url.length);
        if (nextInt < 0) {
            return url[0];
        }
        return url[nextInt];
    }

    /**
     * 客户端用户
     * @return
     */
    public String getClientUser() {
        return clientUser;
    }

    /**
     * 客户端密码
     * @return
     */
    public String getClientPassword() {
        return clientPassword;
    }

    /**
     * 获取代理接口中的所有方法
     *
     * @return
     */
    public Method[] getProxyMethods() {
        return this.type.getMethods();
    }

    /**
     * 指定name获取属性
     *
     * @param name
     * @return
     */
    public String getAnnotationAttributeAsString(String name) {
        return ObjectUtils.toString(getAnnotationAttribute(name));
    }

    /**
     * 指定name获取属性
     *
     * @param name
     * @return
     */
    public Object getAnnotationAttribute(String name) {
        for (AnnotationAttributes attributes : annotationAttributesMap.values()) {
            Object value = attributes.get(name);
            if (ObjectUtils.isNotEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * toString
     *
     * @return
     */
    @Override
    public String toString() {
        return "TargetProxy{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", url='" + Arrays.toString(url) + '\'' +
                ", clientUser='" + clientUser + '\'' +
                ", clientPassword='" + clientPassword + '\'' +
                '}';
    }
}
