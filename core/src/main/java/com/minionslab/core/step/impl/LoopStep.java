package com.minionslab.core.step.impl;

import com.minionslab.core.step.definition.StepDefinition;

import java.util.List;

public class LoopStep {
    
    private String conditionExpr;
    private List<StepDefinition> bodySteps;
    private int maxIterations;
}
