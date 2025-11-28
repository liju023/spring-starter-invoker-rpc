/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i360day.invoker.support;

import com.i360day.invoker.common.ClassUtils;
import com.i360day.invoker.common.TypeUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a remote invocation, providing core method invocation properties
 * in a serializable fashion. Used for RMI and HTTP-based serialization invokers.
 *
 * <p>This is an SPI class, typically not used directly by applications.
 * Can be subclassed for additional invocation parameters.
 *
 * <p>Both {@link RemoteInvocation} and {@link RemoteInvocationResult} are designed
 * for use with standard Java serialization as well as JavaBean-style serialization.
 *
 */
public class RemoteInvocation implements Serializable {

    /**
     * use serialVersionUID from Spring 1.1 for interoperability.
     */
    private static final long serialVersionUID = 6876024250231820554L;


    private String methodName;

    private Type[] parameterTypes;

    private Class<?>[] parameterClazzs;

    private Object[] arguments;

    private Map<String, Serializable> attributes;

    /**
     * Create a new RemoteInvocation for the given AOP method invocation.
     *
     * @param methodInvocation the AOP invocation to convert
     */
    public RemoteInvocation(MethodInvocation methodInvocation) {
        this(
                methodInvocation.getMethod().getName(),
                methodInvocation.getMethod().getParameterTypes(),
                methodInvocation.getMethod().getGenericParameterTypes(),
                methodInvocation.getArguments()
        );
    }

    /**
     * Create a new RemoteInvocation for the given parameters.
     *
     * @param methodName     the name of the method to invoke
     * @param parameterTypes the parameter types of the method
     * @param arguments      the arguments for the invocation
     */
    public RemoteInvocation(String methodName, Class<?>[] parameterClazzs, Type[] parameterTypes, Object[] arguments) {
        this.arguments = arguments;
        this.methodName = methodName;
        this.parameterClazzs = parameterClazzs;
        this.parameterTypes = TypeUtils.resolveToSerializable(parameterTypes);

        //check arguments
        if (this.parameterTypes != null && this.parameterTypes.length > 0) {
            for (int i = 0; i < this.parameterTypes.length; i++) {

                //如果不支持序列化，设置为object.class。未识别的在objectMapper序列化时会抛异常
                if (!Serializable.class.isInstance(this.parameterTypes[i])) {

                    //如果parameterClazzs[i]中的值不是object，则提示使用remoteParam指定类型
                    Assert.isTrue(this.parameterClazzs[i] == Object.class, String.format("%s Serialization not supported，please use @RemoteParam Specify type", this.parameterTypes[i]));
                    this.parameterTypes[i] = Object.class;
                }
            }
        }
    }

    /**
     * Create a new RemoteInvocation for JavaBean-style deserialization
     * (e.g. with Jackson).
     */
    public RemoteInvocation() {
    }


    /**
     * Set the name of the target method.
     * <p>This setter is intended for JavaBean-style deserialization.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Return the name of the target method.
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Set the parameter types of the target method.
     * <p>This setter is intended for JavaBean-style deserialization.
     */
    public void setParameterTypes(Type[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * Return the parameter types of the target method.
     */
    public Type[] getParameterTypes() {
        return this.parameterTypes;
    }

    public Class<?>[] getParameterClazzs() {
        return parameterClazzs;
    }

    public void setParameterClazzs(Class<?>[] parameterClazzs) {
        this.parameterClazzs = parameterClazzs;
    }

    /**
     * Set the arguments for the target method call.
     * <p>This setter is intended for JavaBean-style deserialization.
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Return the arguments for the target method call.
     */
    public Object[] getArguments() {
        return this.arguments;
    }


    /**
     * Add an additional invocation attribute. Useful to add additional
     * invocation context without having to subclass RemoteInvocation.
     * <p>Attribute keys have to be unique, and no overriding of existing
     * attributes is allowed.
     * <p>The implementation avoids to unnecessarily create the attributes
     * Map, to minimize serialization size.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @throws IllegalStateException if the key is already bound
     */
    public void addAttribute(String key, Serializable value) throws IllegalStateException {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        if (this.attributes.containsKey(key)) {
            throw new IllegalStateException("There is already an attribute with key '" + key + "' bound");
        }
        this.attributes.put(key, value);
    }

    /**
     * Retrieve the attribute for the given key, if any.
     * <p>The implementation avoids to unnecessarily create the attributes
     * Map, to minimize serialization size.
     *
     * @param key the attribute key
     * @return the attribute value, or {@code null} if not defined
     */
    @Nullable
    public Serializable getAttribute(String key) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes.get(key);
    }

    /**
     * Set the attributes Map. Only here for special purposes:
     * Preferably, use {@link #addAttribute} and {@link #getAttribute}.
     *
     * @param attributes the attributes Map
     * @see #addAttribute
     * @see #getAttribute
     */
    public void setAttributes(@Nullable Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    /**
     * Return the attributes Map. Mainly here for debugging purposes:
     * Preferably, use {@link #addAttribute} and {@link #getAttribute}.
     *
     * @return the attributes Map, or {@code null} if none created
     * @see #addAttribute
     * @see #getAttribute
     */
    @Nullable
    public Map<String, Serializable> getAttributes() {
        return this.attributes;
    }

    /**
     * 获取方法
     *
     * @param obj
     * @return
     */
    public Method getMethod(Object obj) throws NoSuchMethodException {
        return getMethod(obj.getClass());
    }

    /**
     * 获取方法
     *
     * @param clazz
     * @return
     */
    public Method getMethod(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod(this.methodName, this.parameterClazzs);
    }

    /**
     * Perform this invocation on the given target object.
     * Typically called when a RemoteInvocation is received on the server.
     *
     * @param targetObject the target object to apply the invocation to
     * @return the invocation result
     * @throws NoSuchMethodException     if the method name could not be resolved
     * @throws IllegalAccessException    if the method could not be accessed
     * @throws InvocationTargetException if the method invocation resulted in an exception
     * @see java.lang.reflect.Method#invoke
     */
    public Object invoke(Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = getMethod(targetObject);
        return method.invoke(targetObject, this.arguments);
    }


    @Override
    public String toString() {
        return "RemoteInvocation: method name '" + this.methodName + "'; parameter types " +
                ClassUtils.classNamesToString(this.parameterClazzs);
    }

    /**
     * 获取返回类型
     *
     * @param targetObject
     * @return
     * @throws NoSuchMethodException
     */
    public Type getReturnType(Object targetObject) throws NoSuchMethodException {
        return getMethod(targetObject).getGenericReturnType();
    }
}
