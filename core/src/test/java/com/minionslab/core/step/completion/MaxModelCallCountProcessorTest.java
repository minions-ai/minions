package com.minionslab.core.step.completion;

import com.minionslab.core.model.ModelCall;
import com.minionslab.core.step.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MaxModelCallCountProcessorTest {
    
    List<ModelCall> value = spy(List.of(mock(ModelCall.class), mock(ModelCall.class), mock(ModelCall.class)));
    private MaxModelCallLimitProcessor processor;
    
    private StepCompletionContext context;
    @Mock
    private StepContext stepContext;
    
    @BeforeEach
    void setUp() {
        processor = new MaxModelCallLimitProcessor();
        context = new StepCompletionContext();
        context.setStepContext(stepContext);
        
        
        when(stepContext.getModelCalls()).thenReturn(value);
        
        
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
        doReturn(12).when(value).size();
        StepCompletionContext process = processor.process(context);
        assertNotNull(process);
        assertNotEquals(process.getResults().isEmpty(), true);
    }
    
    @Test
    void processModelCallNumberLessThan10() {
        doReturn(8).when(value).size();
        StepCompletionContext process = processor.process(context);
        assertNotNull(process);
        assertEquals(process.getResults().isEmpty(), true);
    }
    
    
}