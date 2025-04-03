package com.minionslab.core.common.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ValidationAspect {


  @Around("@annotation(org.springframework.validation.annotation.Validated)")
  public Object logValidation(ProceedingJoinPoint joinPoint) throws Throwable {
    log.debug("Validation aspect triggered for: {}", joinPoint.getSignature());
    return joinPoint.proceed();
  }
}
