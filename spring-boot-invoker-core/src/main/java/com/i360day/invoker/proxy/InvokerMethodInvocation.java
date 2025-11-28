package com.i360day.invoker.proxy;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class InvokerMethodInvocation implements MethodInvocation {

    private Method method;
    private Object[] args;
    private Object obj;

    public InvokerMethodInvocation(Object obj, Method method, Object[] args) {
        this.method = method;
        this.args = args;
        this.obj = obj;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return args;
    }

    @Override
    public Object proceed() throws Throwable {
        return method.invoke(obj, args);
    }

    @Override
    public Object getThis() {
        return obj;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }
}
