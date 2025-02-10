package com.hls.minions.core.annotation;


import com.hls.minions.core.service.prompt.ScopeType;
import com.hls.minions.core.service.prompt.SourceType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Points to the place that the system prompt for this agent is stored.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AgentPrompt {

  String value();

  ScopeType scope();

  SourceType source();
}