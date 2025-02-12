package com.hls.minions.core.agent;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AgentPrompt {

  // Defines the source of the prompt (Mongo NoSQL or Mongo Vector)
  SourceType source();

  // The identifier used for lookup (DB Key or Vector Query)
  String value();

  // Defines the scope (System-wide, Tenant-wide, or User-specific)
  ScopeType scope();

}

