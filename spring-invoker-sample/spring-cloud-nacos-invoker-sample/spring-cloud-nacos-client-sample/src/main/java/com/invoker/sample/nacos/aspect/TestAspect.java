package com.invoker.sample.nacos.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TestAspect {


    /** 配置织入点 */
    @Pointcut("@annotation(com.invoker.sample.nacos.aspect.TestAnnotation)")
    public void scopePointCut() {
    }

    @AfterReturning(value = "scopePointCut()", returning = "object")
    public void afterReturning(JoinPoint joinPoint, Object object) {
        System.out.println(object);
    }
}
