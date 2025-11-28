package com.i360day.invoker;

import com.i360day.invoker.annotation.RemoteResponseBody;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 方法源信息
 * @author: liju.z
 * @create: 2021-12-05 17:00
 **/
public class MethodMetadata {
    /**
     * return type
     */
    private transient Type returnType;
    /**
     * return class
     */
    private transient Class<?> returnClazz;
    /**
     * target method
     */
    private transient Method method;
    /**
     * target method args
     */
    private transient Object[] args;

    public MethodMetadata(Type returnType, Class<?> returnClazz, Method method, Object[] args) {
        this.returnType = returnType;
        this.returnClazz = returnClazz;
        this.method = method;
        this.args = args;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Class<?> getReturnClazz() {
        return returnClazz;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    /**
     * create MethodMetadata
     * @param invocation
     * @return
     */
    public static MethodMetadata of(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        return new MethodMetadata(method.getGenericReturnType(), method.getReturnType(), method, invocation.getArguments());
    }

    /**
     * create MethodMetadata
     * @param method
     * @param args
     * @return
     */
    public static MethodMetadata of(Method method, Object [] args) {
        RemoteResponseBody remoteResponseBody = method.getAnnotation(RemoteResponseBody.class);
        if(remoteResponseBody != null){
            return new MethodMetadata(remoteResponseBody.deserialize(), remoteResponseBody.deserialize(), method, args);
        }
        return new MethodMetadata(method.getGenericReturnType(), method.getReturnType(), method, args);
    }
}
