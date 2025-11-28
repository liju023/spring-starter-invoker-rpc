package com.i360day.invoker.common;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * <p> @description:     <p>
 * <p> @author: 胡.青牛   <p>
 * <p> @date: 2019/5/10 0010  15:58
 **/
public class ObjectUtils {

    /**
     * toString
     * @param obj
     * @return
     */
    public static String toString(Object obj){
        if(isEmpty(obj)) return null;
        return obj.toString();
    }

    /**
     * to boolean
     * @param obj
     * @return
     */
    public static Boolean toBoolean(Object obj){
        if(isEmpty(obj)) return false;
        try{
            return obj instanceof Boolean ? (Boolean)obj : Boolean.valueOf(obj.toString());
        }catch (Exception e){
            return false;
        }
    }

    /**
     * to class
     * @param obj
     * @return
     */
    public static Class<?> toClass(Object obj){
        if(isEmpty(obj)) return null;
        try{
            return obj instanceof Class<?> ? (Class<?>) obj : obj.getClass();
        }catch (Exception e){
            return null;
        }
    }

    /**
     * <p> @Description: 两个值是否相等   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/3/22 0022 11:53   <p>
     *
     * <p> @param desc     <p>
     * <p> @param src     <p>
     *
     * <p> @return: boolean     <p>
     **/
    public static boolean isEquals(Object desc, Object src) {
        if (isEmpty(desc)) return false;
        if (isEmpty(src)) return false;
        return desc.equals(src);
    }

    /**
     * <p> @Description: 判断对象是否为空   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/3/22 0022 11:53   <p>
     *
     * <p> @param obj     <p>
     *
     * <p> @return: boolean     <p>
     **/
    public static boolean isEmpty(Object obj) {
        if (obj == null){
            return true;
        }
        else if (obj instanceof CharSequence charSequence) {
            return charSequence.isEmpty();
        }
        else if(obj instanceof Collection<?> collection){
            return collection.isEmpty();
        }
        else if(obj instanceof Map<?,?> map){
            return map.isEmpty();
        }
        else if (obj instanceof Optional<?> optional) {
            return optional.isEmpty();
        }
        else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        return false;
    }

    /**
     * <p> @Description: 判断对象是否为空   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/3/22 0022 11:53   <p>
     *
     * <p> @param obj     <p>
     *
     * <p> @return: boolean     <p>
     **/
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 判断包路径是否为空 <p>
     *
     * <p> @Date  22:48 <p>
     *
     * <p> @Param [obj] <p>
     *
     * <p> @return [obj] <p>
     **/
    public static boolean isPackage(Object obj) {
        if (obj == null) {
            return true;
        } else if (!(obj instanceof String[])) {
            return true;
        } else if (((String[]) obj).length == 0) {
            return true;
        }
        return false;
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 判断数组是否为空 <p>
     *
     * <p> @Date  9:14 <p>
     *
     * <p> @Param [obj] <p>
     *
     * <p> @return [obj] <p>
     **/
    public static boolean isArrayEmpty(Object... objs) {
        if (objs == null) {
            return true;
        } else if (objs.length == 0) {
            return true;
        }
        //数组中所有数据都为空
        long count = Arrays.asList(objs).stream().filter(obj -> ObjectUtils.isNotEmpty(obj)).count();
        if(count == 0){
            return true;
        }
        return false;
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 是否调用toString方法 <p>
     *
     * <p> @Date  21:33 <p>
     *
     * <p> @Param [invocation] <p>
     *
     * <p> @return Object <p>
     **/
    public static Object isToString(Object obj, MethodInvocation invocation) {
        String methodName = invocation.getMethod().getName();
        if ("equals".equals(methodName)) {
            Object[] args = invocation.getArguments();
            try {
                Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return obj.equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(methodName)) {
            return obj.hashCode();
        } else if ("toString".equals(methodName)) {
            return obj.toString();
        }
        return null;
    }


    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 是否调用toString方法 <p>
     *
     * <p> @Date  21:33 <p>
     *
     * <p> @Param [invocation] <p>
     *
     * <p> @return Object <p>
     **/
    public static Object isToString(Object obj, Method method, Object[] args) {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return obj.equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return obj.equals(args[0]);
            }
        } else if ("hashCode".equals(method.getName())) {
            return obj.hashCode();
        } else if ("toString".equals(method.getName())) {
            return obj.toString();
        }
        return null;
    }

    public static boolean startsWith(String src, String str){
        return isNotEmpty(src) && src.startsWith(str);
    }
}
