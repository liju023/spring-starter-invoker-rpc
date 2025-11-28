package com.i360day.invoker.common;

import com.i360day.invoker.exception.InvokerException;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * class 工具类
 *
 * @author: 胡.青牛
 * @date: 2019/4/30 0030  16:55
 **/
public class ClassUtils {

    private final static String PACKAGE_SEPARATOR = ".";

    /**
     * Return the user-defined class for the given class: usually simply the given
     * class, but the original class in case of a CGLIB-generated subclass.
     * @param clazz the class to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz.getName().contains("$$")) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }

    /**
     * 构造方法中是否包含指定class
     * @param objClazz
     * @param targetClazz
     * @return
     */
    public static boolean isConstructorContainsClass(Class<?> objClazz, Class<?> targetClazz){
        return Stream.of(objClazz.getConstructors()).filter(f -> {
            return Stream.of(f.getParameterTypes()).filter(t -> t.equals(targetClazz)).count() > 0;
        }).count() > 0;
    }

    /**
     * 获取构造方法中包含class的位置
     * @param objClazz
     * @param targetClazz
     * @return
     */
    public static int[] getConstructorClassIndex(Class<?> objClazz, Class<?> targetClazz){
        Constructor<?>[] constructors = objClazz.getConstructors();
        return IntStream.range(0, constructors.length).flatMap(i -> {
            Class<?>[] parameterTypes = constructors[i].getParameterTypes();
            return IntStream.range(0, parameterTypes.length).filter(index -> parameterTypes[index].equals(targetClazz));
        }).toArray();
    }

    /**
     * 获取类加载器
     *
     * @return
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 反射一个class ,如果没有则返回空
     *
     * @param className
     * @return
     */
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className, true, getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvokerException(e.getMessage());
        }
    }

    /**
     * 反射数组中的class
     *
     * @param className
     * @return
     */
    public static Class<?>[] getClass(String... className) {
        Assert.notNull(className, "clazzName must not be null");
        Set<Class<?>> classSet = new LinkedHashSet<>();
        for (String clazz : className) {
            classSet.add(getClass(clazz));
        }
        return classSet.toArray(new Class<?>[classSet.size()]);
    }

    /**
     * 当前class是否包含指定interface
     * @param currentClass
     * @param interfaceClass
     * @return
     */
    public static boolean isClassContainInterface(Class<?> currentClass, Class<?> interfaceClass){
        return Stream.of(currentClass.getInterfaces()).filter(f -> f.equals(interfaceClass)).count() > 0;
    }

    /**
     * 获取当前对象的class
     *
     * @param target
     * @return
     */
    public static Class<?> getTargetClass(Object target) {
        Assert.notNull(target, "target is null");

        if (target instanceof Class<?>) {
            return (Class<?>) target;
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null) {
            targetClass = target.getClass();
        }
        return targetClass;
    }

    /**
     * @param className
     * @Description: 返回class的类名
     * @author: 胡.青牛
     * @Date: 2019/5/11 0011 10:44
     * @return:
     **/
    public static String getClassToClassName(String className) {
        Assert.notNull(className, "className must not be null");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1);
    }

    /**
     * @param clazz
     * @Description: 返回class的类名
     * @author: 胡.青牛
     * @Date: 2019/5/11 0011 10:44
     * @return:
     **/
    public static String getClassToClassName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1);
    }

    /**
     * @param clazz
     * @Description: 获取class的接口
     * @author: 胡.青牛
     * @Date: 2019/5/11 0011 10:42
     * @return:
     **/
    public static Class[] getClassInterfaces(Class<?> clazz) {
        Assert.notNull(clazz, "clazz must not be null");
        return clazz.getInterfaces();
    }

    /**
     * 首字母转小写
     *
     * @param className
     * @return
     */
    public static String getClassNameToLowerCaseFirstOne(String className) {
        if (Character.isLowerCase(className.charAt(0))) {
            return className;
        }
        else {
            return Character.toLowerCase(className.charAt(0)) + className.substring(1);
        }
    }

    /**
     * 首字母转小写
     *
     * @param clazz
     * @return
     */
    public static String getClassNameToLowerCaseFirstOne(Class<?> clazz) {
        String className = getClassToClassName(clazz);
        if (Character.isLowerCase(className.charAt(0)))
            return className;
        else
            return (new StringBuilder()).append(Character.toLowerCase(className.charAt(0))).append(className.substring(1)).toString();
    }

    /**
     * @param className
     * @return
     * @method: toUpperCaseFirstOne
     * @Description: 首字母转大写
     * @author z.liju
     * <p>
     * 2019年5月3日 上午11:01:40
     * <p>
     * return parameter：String
     */
    public static String getClassNameToUpperCaseFirstOne(String className) {
        if (ObjectUtils.isEmpty(className)) return className;

        if (Character.isUpperCase(className.charAt(0))) {
            return className;
        } else {
            return (new StringBuilder())
                    .append(Character.toUpperCase(className.charAt(0)))
                    .append(className.substring(1)).toString();
        }
    }

    /**
     * 获取默认类加载器
     *
     * @return
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    /**
     * 将class转换成string
     *
     * @param classes
     * @return
     */
    public static String classNamesToString(Class<?>... classes) {
        return classNamesToString(Arrays.asList(classes));
    }

    /**
     * 将type数组转换成string数组
     *
     * @param types
     * @return
     */
    public static String[] typeToStringArray(Type... types) {
        return typeToStringArray(Arrays.asList(types));
    }

    /**
     * 将type集合转换成string数组
     *
     * @param types
     * @return
     */
    public static String[] typeToStringArray(@Nullable Collection<Type> types) {
        if (CollectionUtils.isEmpty(types)) {
            return new String[]{};
        } else {
            return types.stream().map(m -> m.getTypeName()).toArray(String[]::new);
        }
    }

    /**
     * @param classes
     * @return
     */
    public static String classNamesToString(@Nullable Collection<Class<?>> classes) {
        if (CollectionUtils.isEmpty(classes)) {
            return "[]";
        } else {
            StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
            Iterator<Class<?>> iterator = classes.iterator();

            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                stringJoiner.add(clazz.getName());
            }
            return stringJoiner.toString();
        }
    }
}
