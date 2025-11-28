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

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

/**
 * Default implementation of the {@link RemoteInvocationFactory} interface.
 * Simply creates a new standard {@link RemoteInvocation} object.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public class JavaRemoteInvocationFactory implements RemoteInvocationFactory {

    @Override
    public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
        return new RemoteInvocation(methodInvocation);
    }

    @Override
    public RemoteInvocation doReadRemoteInvocation(ObjectInputStream ois) throws IOException, ClassNotFoundException {

        Object obj = ois.readObject();
        if (!(obj instanceof RemoteInvocation)) {
            throw new RemoteException("Deserialized object needs to be assignable to type [" +
                    RemoteInvocation.class.getName() + "]: " + ClassUtils.getDescriptiveType(obj));
        }
        return (RemoteInvocation) obj;
    }
}
