package com.api.wishoria.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Pointcut("execution(public * com.api.wishoria.service..*(..))")
    public void servicePublicMethods() {}

    @Around("servicePublicMethods()")
    public Object logServiceMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        log.debug("[{}#{}] started", className, methodName);
        try {
            Object result = proceedingJoinPoint.proceed();
            log.debug("[{}#{}] completed in {}ms", className, methodName, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("[{}#{}] failed after {}ms: {}", className, methodName, System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        }
    }
}
