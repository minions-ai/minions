package com.minionslab.core.step.impl;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class SetEntityStep extends AbstractStep {
    private String entity;
    private Map<String, String> keyValueMap;


}

