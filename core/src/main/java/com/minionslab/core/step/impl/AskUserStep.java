package com.minionslab.core.step.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AskUserStep extends AbstractStep {
    private String question;
    private String inputType;
    private boolean optional;
    

}
