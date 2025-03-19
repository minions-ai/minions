package com.minionslab.core.common.annotation;

import com.minionslab.core.domain.enums.MinionType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a Minion. Extends VersionedBean with additional minion-specific properties.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Minion {

  /**
   * The name of the minion.
   *
   * @return the name of the minion
   */
  String name();

  /**
   * The version of the minion.
   *
   * @return the version of the minion
   */
  String version() default "1";

  /**
   * The role of the minion.
   *
   * @return the role of the minion
   */
  MinionType type() default MinionType.USER_DEFINED_AGENT;


} 