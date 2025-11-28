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

import com.i360day.invoker.annotation.RemoteDisable;
import com.i360day.invoker.codes.encoder.Encoder;
import com.i360day.invoker.exception.RemoteDisableException;
import com.i360day.invoker.security.InvokerSecurityAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Abstract base class for remote service exporters that are based
 * on deserialization of {@link RemoteInvocation} objects.
 *
 * <p>Provides a "remoteInvocationExecutor" property, with a
 * {@link DefaultRemoteInvocationExecutor} as default strategy.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see RemoteInvocationExecutor
 * @see DefaultRemoteInvocationExecutor
 */
public abstract class RemoteInvocationBasedExporter extends RemoteExporter {
	private Class<? extends Encoder> encoderClass;
	private RemoteInvocationExecutor remoteInvocationExecutor = new DefaultRemoteInvocationExecutor(new InvokerSecurityAdapter() {
		@Override
		public void check(HttpServletRequest request, HttpServletResponse response, RemoteInvocation remoteInvocation) {
			//ignore
		}
	});

	public Class<? extends Encoder> getEncoderClass() {
		return encoderClass;
	}

	public void setEncoderClass(Class<? extends Encoder> encoderClass) {
		this.encoderClass = encoderClass;
	}

	/**
	 * Set the RemoteInvocationExecutor to use for this exporter.
	 * Default is a DefaultRemoteInvocationExecutor.
	 * <p>A custom invocation executor can extract further context information
	 * from the invocation, for example user credentials.
	 */
	public void setRemoteInvocationExecutor(RemoteInvocationExecutor remoteInvocationExecutor) {
		this.remoteInvocationExecutor = remoteInvocationExecutor;
	}

	/**
	 * Return the RemoteInvocationExecutor used by this exporter.
	 */
	public RemoteInvocationExecutor getRemoteInvocationExecutor() {
		return this.remoteInvocationExecutor;
	}


	/**
	 * Apply the given remote invocation to the given target object.
	 * The default implementation delegates to the RemoteInvocationExecutor.
	 * <p>Can be overridden in subclasses for custom invocation behavior,
	 * possibly for applying additional invocation parameters from a
	 * custom RemoteInvocation subclass. Note that it is preferable to use
	 * a custom RemoteInvocationExecutor which is a reusable strategy.
	 * @param invocation the remote invocation
	 * @param targetObject the target object to apply the invocation to
	 * @return the invocation result
	 * @throws NoSuchMethodException if the method name could not be resolved
	 * @throws IllegalAccessException if the method could not be accessed
	 * @throws InvocationTargetException if the method invocation resulted in an exception
	 * @see RemoteInvocationExecutor#invoke
	 */
	protected Object invoke(RemoteInvocation invocation, Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		if (logger.isTraceEnabled()) {
			logger.trace("Executing " + invocation);
		}
		try {
			//接口中的标记
			Method interfaceMethod = invocation.getMethod(getServiceInterface());
			if(interfaceMethod.getAnnotation(RemoteDisable.class) != null) {
				throw new RemoteDisableException(String.format("%s interface %s method is disabled", getServiceInterface(), interfaceMethod.getName()));
			}

			//实现类中的标记
			Method method = invocation.getMethod(targetObject);
			if(method.getAnnotation(RemoteDisable.class) != null) {
				throw new RemoteDisableException(String.format("%s service %s method is disabled", getServiceInterface(), method.getName()));
			}

			return getRemoteInvocationExecutor().invoke(invocation, targetObject, getServiceInterface());
		}
		catch (NoSuchMethodException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not find target method for " + invocation, ex);
			}
			throw ex;
		}
		catch (IllegalAccessException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not access target method for " + invocation, ex);
			}
			throw ex;
		}
		catch (InvocationTargetException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Target method failed for " + invocation, ex.getTargetException());
			}
			throw ex;
		}
	}

	/**
	 * Apply the given remote invocation to the given target object, wrapping
	 * the invocation result in a serializable RemoteInvocationResult object.
	 * The default implementation creates a plain RemoteInvocationResult.
	 * <p>Can be overridden in subclasses for custom invocation behavior,
	 * for example to return additional context information. Note that this
	 * is not covered by the RemoteInvocationExecutor strategy!
	 * @param invocation the remote invocation
	 * @param targetObject the target object to apply the invocation to
	 * @return the invocation result
	 * @see #invoke
	 */
	protected RemoteInvocationResult invokeAndCreateResult(RemoteInvocation invocation, Object targetObject) {
		try {
			Object value = invoke(invocation, targetObject);
			return new RemoteInvocationResult(value);
		}
		catch (Throwable ex) {
			return new RemoteInvocationResult(ex);
		}
	}

}
