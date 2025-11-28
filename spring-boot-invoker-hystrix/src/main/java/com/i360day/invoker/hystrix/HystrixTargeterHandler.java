package com.i360day.invoker.hystrix;

import com.i360day.invoker.annotation.RemoteDisable;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.exception.InvokerException;
import com.i360day.invoker.exception.RemoteDisableException;
import com.i360day.invoker.proxy.TargetProxy;
import com.netflix.hystrix.HystrixCommand;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author liju.z
 */
public class HystrixTargeterHandler implements MethodInterceptor {

    private FallbackFactory<?> fallbackFactory;
    private Map<Method, MethodInterceptor> dispatchMethod;
    private Map<Method, Method> fallbackMethodMap;
    private Map<Method, HystrixCommand.Setter> methodSetterMap;
    private TargetProxy target;

    public HystrixTargeterHandler(SetterFactory setterFactory, FallbackFactory<?> fallbackFactory, Map<Method, MethodInterceptor> dispatchMethod, TargetProxy target) {
        this.fallbackFactory = fallbackFactory;
        this.dispatchMethod = dispatchMethod;
        this.fallbackMethodMap = toFallbackMethodMap(dispatchMethod.keySet());
        this.methodSetterMap = dispatchMethod.keySet().stream().collect(Collectors.toMap(method -> method, method -> setterFactory.create(target, method)));
        this.target = target;
    }

    private Map<Method, Method> toFallbackMethodMap(Set<Method> methodInterceptors) {
        Map<Method, Method> fallbackMethodMap = new LinkedHashMap<>();
        for (Method method : methodInterceptors) {
            fallbackMethodMap.put(method, method);
        }
        return fallbackMethodMap;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        Object toString = ObjectUtils.isToString(this, method, args);
        if (ObjectUtils.isNotEmpty(toString)) return toString;

        HystrixCommand<Object> hystrixCommand = new HystrixCommand<>(methodSetterMap.get(method)) {
            @Override
            protected Object run() {
                try {

                    if(method.getAnnotation(RemoteDisable.class) != null) {
                        throw new RemoteDisableException(String.format("%s %s method is disabled", method.getDeclaringClass(), method.getName()));
                    }

                    return dispatchMethod.get(method).invoke(invocation);
                } catch (Throwable throwable) {
                    throw new InvokerException(throwable);
                }
            }

            @Override
            protected Object getFallback() {
                if (fallbackFactory == null) {
                    return super.getFallback();
                }
                try {
                    Object fallback = fallbackFactory.create(getExecutionException());
                    if (fallback == null) return super.getFallback();
                    //get fallback method
                    Object result = fallbackMethodMap.get(method).invoke(fallback, args);
                    if (isReturnsHystrixCommand(method)) {
                        return ((HystrixCommand) result).execute();
                    } else if (isReturnsObservable(method)) {
                        // Create a cold Observable
                        return ((Observable) result).toBlocking().first();
                    } else if (isReturnsSingle(method)) {
                        // Create a cold Observable as a Single
                        return ((Single) result).toObservable().toBlocking().first();
                    } else if (isReturnsCompletable(method)) {
                        ((Completable) result).await();
                        return null;
                    } else if (isReturnsCompletableFuture(method)) {
                        return ((Future) result).get();
                    } else {
                        return result;
                    }
                } catch (IllegalAccessException e) {
                    // shouldn't happen as method is public due to being an interface
                    throw new AssertionError(e);
                } catch (InvocationTargetException | ExecutionException e) {
                    // Exceptions on fallback are tossed by Hystrix
                    throw new AssertionError(e.getCause());
                } catch (InterruptedException e) {
                    // Exceptions on fallback are tossed by Hystrix
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e.getCause());
                } catch (IllegalArgumentException e) {
                    //object is not an instance of declaring class
                    throw new AssertionError(e.getCause());
                } catch (InvokerException e) {
                    throw new AssertionError(e);
                }
            }
        };

        if (isDefault(method)) {
            return hystrixCommand.execute();
        } else if (isReturnsHystrixCommand(method)) {
            return hystrixCommand;
        } else if (isReturnsObservable(method)) {
            // Create a cold Observable
            return hystrixCommand.toObservable();
        } else if (isReturnsSingle(method)) {
            // Create a cold Observable as a Single
            return hystrixCommand.toObservable().toSingle();
        } else if (isReturnsCompletable(method)) {
            return hystrixCommand.toObservable().toCompletable();
        }
//        else if (isReturnsCompletableFuture(method)) {
//            return new CompletableFuture<>(hystrixCommand);
//        }
        return hystrixCommand.execute();
    }

    private boolean isReturnsCompletable(Method method) {
        return Completable.class.isAssignableFrom(method.getReturnType());
    }

    private boolean isReturnsHystrixCommand(Method method) {
        return HystrixCommand.class.isAssignableFrom(method.getReturnType());
    }

    private boolean isReturnsObservable(Method method) {
        return Observable.class.isAssignableFrom(method.getReturnType());
    }

    private boolean isReturnsCompletableFuture(Method method) {
        return CompletableFuture.class.isAssignableFrom(method.getReturnType());
    }

    private boolean isReturnsSingle(Method method) {
        return Single.class.isAssignableFrom(method.getReturnType());
    }

    /**
     * Identifies a method as a default instance method.
     */
    private static boolean isDefault(Method method) {
        // Default methods are public non-abstract, non-synthetic, and non-static instance methods
        // declared in an interface.
        // method.isDefault() is not sufficient for our usage as it does not check
        // for synthetic methods. As a result, it picks up overridden methods as well as actual default
        // methods.
        final int SYNTHETIC = 0x00001000;
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC | SYNTHETIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }

    @Override
    public String toString() {
        return target.toString();
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
