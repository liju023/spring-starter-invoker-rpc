/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i360day.invoker.executor;

import com.i360day.invoker.RequestTemplate;
import com.i360day.invoker.http.Response;

import java.io.IOException;

/**
 * Strategy interface for actual execution of an HTTP invoker request.
 * Used by HttpInvokerClientInterceptor and its subclass
 * HttpInvokerProxyFactoryBean.
 *
 * <p>Two implementations are provided out of the box:
 * <ul>
 * <li><b>{@code SimpleHttpInvokerRequestExecutor}:</b>
 * Uses JDK facilities to execute POST requests, without support
 * for HTTP authentication or advanced configuration options.
 * <li><b>{@code HttpComponentsHttpInvokerRequestExecutor}:</b>
 * Uses Apache's Commons HttpClient to execute POST requests,
 * allowing to use a preconfigured HttpClient instance
 * (potentially with authentication, HTTP connection pooling, etc).
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
@FunctionalInterface
public interface InvokerRequestExecutor {
	/**
	 * Execute a request to send the given remote invocation.
	 * target service
	 *
	 * @param requestTemplate the RemoteInvocation to execute
	 * @return the RemoteInvocationResult object
	 * @throws IOException            if thrown by I/O operations
	 * @throws ClassNotFoundException if thrown during deserialization
	 * @throws Exception              in case of general errors
	 */
	Response executeRequest(RequestTemplate requestTemplate) throws Exception;

}
