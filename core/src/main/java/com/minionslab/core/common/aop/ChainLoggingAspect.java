package com.minionslab.core.common.aop;

import com.minionslab.core.common.logging.LoggingTopics;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging Chain operations at debug level.
 */
@Slf4j(topic = LoggingTopics.CHAIN)
@Aspect
@Component
public class ChainLoggingAspect {

    @Around("execution(* com.minionslab.core.common.chain.Chain.process(..)) || execution(* com.minionslab.core.common.chain.Chain.processAsync(..))")
    public Object logChainProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        String chainClass = joinPoint.getTarget().getClass().getSimpleName();
        Object input = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        try {
            log.debug("[{}] Starting {} with input: {}", chainClass, method, input);
            Object result = joinPoint.proceed();
            log.debug("[{}] {} completed with results: {}", chainClass, method, result);
            return result;
        } catch (Throwable t) {
            log.error("[{}] {} failed with error: {}", chainClass, method, t.getMessage(), t);
            throw t;
        }
    }
} 