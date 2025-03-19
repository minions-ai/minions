package com.minionslab.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a Toolbox. Extends VersionedBean with additional toolbox-specific properties.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Toolbox {

  /**
   * The name of the toolbox.
   *
   * @return the name of the toolbox
   */
  String name();

  /**
   * The version of the toolbox.
   *
   * @return the version of the toolbox
   */
  String version();

  /**
   * The category of the toolbox.
   *
   * @return the category of the toolbox
   */
  String[] categories();

}