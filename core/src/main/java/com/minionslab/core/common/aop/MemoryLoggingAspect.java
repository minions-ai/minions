package com.minionslab.core.common.aop;

import com.minionslab.core.common.logging.LoggingTopics;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging Memory operations at debug level.
 */
@Slf4j(topic = LoggingTopics.MEMORY_STRATEGY)
@Aspect
@Component
public class MemoryLoggingAspect {

    @Around("execution(* com.minionslab.core.memory.AbstractMemory.*(..)) || " +
            "execution(* com.minionslab.core.memory.MemoryManager.*(..)) || " +
            "execution(* com.minionslab.core.memory.MemoryFactory.*(..))")
    public Object logMemoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        try {
            log.debug("[{}] Starting {} with args: {}", className, method, args);
            Object result = joinPoint.proceed();
            log.debug("[{}] {} completed with results: {}", className, method, result);
            return result;
        } catch (Throwable t) {
            log.error("[{}] {} failed with error: {}", className, method, t.getMessage(), t);
            throw t;
        }
    }
} 