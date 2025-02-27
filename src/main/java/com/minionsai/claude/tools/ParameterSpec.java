package com.minionsai.claude.tools;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParameterSpec {

  private final String name;
  private final String description;
  private final ParameterType type;
  private final boolean required;
  private final Object defaultValue;
}