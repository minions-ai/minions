package com.minionslab.core.step.customizer;

import com.minionslab.core.step.Step;

/**
 * StepCustomizer allows dynamic modification or decoration of steps at runtime.
 * <p>
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Implement this interface to provide custom logic for modifying, decorating, or validating steps.</li>
 *   <li>Override {@link #accepts(Step)} to control which steps are eligible for customization.</li>
 * </ul>
 * <b>Usage:</b> Use StepCustomizer to inject dynamic behavior, validation, or metadata into steps during workflow execution.
 */
public interface StepCustomizer<T extends Step> {
    
    default boolean accepts(T step){
        return step != null;
    }
    
    void customize(T step);
}
