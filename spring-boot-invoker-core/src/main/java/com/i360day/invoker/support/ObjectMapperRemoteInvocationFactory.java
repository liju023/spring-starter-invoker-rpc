/*
 * Copyright 2002-2012 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i360day.invoker.annotation.RemoteRequestParam;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.exception.RemoteException;
import com.i360day.invoker.exception.RemoteSerializableException;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

/**
 * RemoteInvocation 编码
 *
 * @author liju.z
 */
public class ObjectMapperRemoteInvocationFactory implements RemoteInvocationFactory {

    private ObjectMapper objectMapper;

    public ObjectMapperRemoteInvocationFactory() {
        this(new ObjectMapper());
    }

    public ObjectMapperRemoteInvocationFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 根据代理MethodInvocation创建RemoteInvocation
     *
     * @param methodInvocation the original AOP MethodInvocation object
     * @return
     * @throws IOException
     */
    @Override
    public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) throws IOException {
        Method method = methodInvocation.getMethod();

        //参数数据
        Object[] arguments = methodInvocation.getArguments();
        if (ObjectUtils.isNotEmpty(arguments)) {
            arguments = methodInvocation.getArguments().clone();
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = objectMapper.writeValueAsBytes(arguments[i]);
            }
        }

        //参数类型
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (ObjectUtils.isNotEmpty(parameterAnnotations) && ObjectUtils.isNotEmpty(genericParameterTypes)) {
            for (int i = 0; i < parameterAnnotations.length; i++) {
                if (parameterAnnotations[i].length == 0) continue;

                //获取第一个RemoteParam
                Optional<RemoteRequestParam> optional = Arrays.stream(parameterAnnotations[i])
                        .filter(f -> f.annotationType() == RemoteRequestParam.class)
                        .map(m -> (RemoteRequestParam) m)
                        .findFirst();
                if (optional.isPresent()) {
                    genericParameterTypes[i] = optional.get().serialize();
                }
            }
        }
        return new RemoteInvocation(method.getName(), method.getParameterTypes(), genericParameterTypes, arguments);
    }

    /**
     * ObjectInputStream读取RemoteInvocation
     *
     * @param ois the ObjectInputStream to read from
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public RemoteInvocation doReadRemoteInvocation(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Object obj = ois.readObject();
        if (!(obj instanceof RemoteInvocation)) {
            throw new RemoteException("Deserialized object needs to be assignable to type [" + RemoteInvocation.class.getName() + "]: " + ClassUtils.getDescriptiveType(obj));
        }
        RemoteInvocation remoteInvocation = (RemoteInvocation) obj;
        Object[] arguments = remoteInvocation.getArguments();
        Type[] parameterTypes = remoteInvocation.getParameterTypes();

        if (ObjectUtils.isNotEmpty(arguments)) {
            for (int i = 0; i < arguments.length; i++) {
                if (ObjectUtils.isEmpty(arguments[i])) {
                    arguments[i] = null;
                } else if (arguments[i] instanceof byte[]) {
                    arguments[i] = objectMapper.readValue((byte[]) arguments[i], InvokerConstant.getJavaType(parameterTypes[i]));
                } else {
                    throw new RemoteSerializableException("Unable to recognize packet content");
                }
            }
        }
        return remoteInvocation;
    }
}
