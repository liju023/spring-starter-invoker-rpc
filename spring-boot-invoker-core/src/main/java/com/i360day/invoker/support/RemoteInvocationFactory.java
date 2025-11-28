/*
 * Copyright 2002-2007 the original author or authors.
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

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Strategy interface for creating a {@link RemoteInvocation} from an AOP Alliance
 * {@link org.aopalliance.intercept.MethodInvocation}.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JavaRemoteInvocationFactory
 */
public interface RemoteInvocationFactory {

	/**
	 * Create a serializable RemoteInvocation object from the given AOP
	 * MethodInvocation.
	 * <p>Can be implemented to add custom context information to the
	 * remote invocation, for example user credentials.
	 * @param methodInvocation the original AOP MethodInvocation object
	 * @return the RemoteInvocation object
	 */
	RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) throws IOException;

	/**
	 * Perform the actual reading of an invocation result object from the
	 * given ObjectInputStream.
	 * <p>The default implementation simply calls
	 * {@link java.io.ObjectInputStream#readObject()}.
	 * Can be overridden for deserialization of a custom wrapper object rather
	 * than the plain invocation, for example an encryption-aware holder.
	 * @param ois the ObjectInputStream to read from
	 * @return the RemoteInvocationResult object
	 * @throws java.io.IOException in case of I/O failure
	 * @throws ClassNotFoundException if case of a transferred class not
	 * being found in the local ClassLoader
	 */
	RemoteInvocation doReadRemoteInvocation(ObjectInputStream ois) throws IOException, ClassNotFoundException;
}
