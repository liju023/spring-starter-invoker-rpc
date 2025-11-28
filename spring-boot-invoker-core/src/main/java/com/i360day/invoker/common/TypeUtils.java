/*
 * Copyright (c) 1994, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.i360day.invoker.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author: liju.z
 * @date: 2023/5/19 9:16
 */
public class TypeUtils {

    /**
     *  指定class创建type
     * @param clazz
     * @param types
     * @return
     * @param <T>
     */
    public static <T> Type createClassType(Class<T> clazz, Type ...types){
        return $Feign$Types.createClassType(clazz, types);
    }

    /**
     * 将type转换成可序列化的type
     *
     * @param type
     * @return
     */
    public static Type resolveToSerializable(Type type) {
        return $Feign$Types.resolveToSerializable(type);
    }

    /**
     * 将type转换成可序列化的type
     *
     * @param types
     * @return
     */
    public static Type[] resolveToSerializable(Type ...types) {
        return $Feign$Types.resolveToSerializable(types);
    }

    /**
     * 获取类型
     *
     * @param type
     * @return
     */
    public static ParameterizedType toParameterizedType(Type type) {
        ParameterizedType result = null;
        if (type instanceof ParameterizedType) {
            result = (ParameterizedType) type;
        } else if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            Type genericSuper = clazz.getGenericSuperclass();
            if (null == genericSuper || Object.class.equals(genericSuper)) {
                // 如果类没有父类，而是实现一些定义好的泛型接口，则取接口的Type
                final Type[] genericInterfaces = clazz.getGenericInterfaces();
                if (genericInterfaces != null && genericInterfaces.length > 0) {
                    // 默认取第一个实现接口的泛型Type
                    genericSuper = genericInterfaces[0];
                }
            }
            result = toParameterizedType(genericSuper);
        }
        return result;
    }

    /**
     * 获取实际类型参数
     * @param type
     * @return
     */
    public static Type getActualTypeArguments(Type type) {
        ParameterizedType parameterizedType = toParameterizedType(type);
        if (parameterizedType == null) {
            return type;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments == null || actualTypeArguments.length == 0) {
            return type;
        }
        return actualTypeArguments[0];
    }

}
