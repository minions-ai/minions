package com.minionslab.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.ai.model.function.FunctionCallback;

/**
 * Concrete implementation of AbstractMinion that provides default behavior.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Minion extends AbstractMinion {

  @Override
  protected FunctionCallback[] getAvailableTools() {
    return new FunctionCallback[0];
  }


}