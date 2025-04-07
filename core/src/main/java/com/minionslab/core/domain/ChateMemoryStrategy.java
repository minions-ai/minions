package com.minionslab.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChateMemoryStrategy {

  private ChatMemoryStrategyType type;
  private Boolean mandatory;

}
