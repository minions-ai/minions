package com.minionslab.core.step.completion;

import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallStatus;
import com.minionslab.core.step.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MaxModelCallCountProcessor}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Processes model call count and enforces max limit</li>
 * </ul>
 * <p>
 * Setup: Mocks StepContext and ModelCall. Initializes MaxModelCallCountProcessor.
 */
@ExtendWith(MockitoExtension.class)
class MaxModelCallCountProcessorTest {
    
    List<ModelCall> modelCallList;
    @Mock(strictness = Mock.Strictness.LENIENT)
    ModelCall mock;
    private MaxModelCallLimitProcessor processor;
    private StepCompletionContext context = spy(new StepCompletionContext());
    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepContext stepContext;
    
    @BeforeEach
    void setUp() {
        processor = new MaxModelCallLimitProcessor();
        context.setStepContext(stepContext);
        modelCallList = new ArrayList<>();
        
        
    }
    
    @Test
    void testAcceptsStepCompletionContext() {
        assertTrue(processor.accepts(context));
    }
    
    @Test
    void testAcceptsDoesNotAcceptNull() {
        assertFalse(processor.accepts(null));
    }
    
    
    @Test
    void processModelCallNumberGreaterThan10() {
        doReturn(stepContext).when(context).getStepContext();
        when(stepContext.getModelCalls()).thenReturn(modelCallList);
        ModelCall mock = mock(ModelCall.class);
        when(mock.getStatus()).thenReturn(ModelCallStatus.FAILED);
        for (int i = 0; i < 10; i++)
            modelCallList.add(mock);
        ReflectionTestUtils.setField(processor, "maxModelCallLimit", 2);
        StepCompletionContext process = processor.process(context);
        assertNotNull(process);
        assertEquals(process.getResults().isEmpty(), false);
        assertTrue(process.getResults().stream().anyMatch(result -> ((ProcessResult) result).getResults().stream().anyMatch(outcome -> ((StepCompletionOutcome) outcome).equals(StepCompletionOutcome.COMPLETE))));
    }
    
    /**
     * Tests that process enforces the max model call limit and throws if exceeded.
     * Setup: StepContext returns a list of ModelCalls.
     * Expected: Throws exception if model call count exceeds limit.
     */
    @Test
    void processModelCallNumberLessThan10() {
        when(mock.getStatus()).thenReturn(ModelCallStatus.FAILED);
        for (int i = 0; i < 5; i++)
            modelCallList.add(mock);
        StepCompletionContext process = processor.process(context);
        assertNotNull(process);
        assertEquals(process.getResults().isEmpty(), false);
        assertTrue(process.getResults().stream().anyMatch(result -> ((ProcessResult) result).getResults().stream().anyMatch(outcome -> ((StepCompletionOutcome) outcome).equals(StepCompletionOutcome.COMPLETE))));
    }
    
    
}