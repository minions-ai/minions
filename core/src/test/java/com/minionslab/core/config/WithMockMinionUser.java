package com.minionslab.core.config;

import com.minionslab.core.test.TestConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMinionUserSecurityContextFactory.class)
public @interface WithMockMinionUser {

  String username() default TestConstants.TEST_USERNAME;

  String[] roles();

  String tenantId() default TestConstants.TEST_TENANT_ID;

  String environmentId() default TestConstants.TEST_ENVIRONMENT_ID;
} 