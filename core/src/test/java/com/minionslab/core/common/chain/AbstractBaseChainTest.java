package com.minionslab.core.common.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractBaseChainTest {
    private TestChain chain;
    private Processor<ProcessContext> p1, p2, p3;
    private ProcessContext context;
    
    // Simple concrete subclass for testing
    static class TestChain extends AbstractBaseChain<Processor,ProcessContext> {
        public TestChain() {
            super(mock(ObjectProvider.class),mock(ObjectProvider.class));
        }
        
        @Override
        protected void registerProcessors() {
            // no-op for test
        }
        
        /**
         * Returns true if any processor in the chain accepts the given context.
         *
         * @param context the context to check
         * @return true if any processor accepts, false otherwise
         */
        @Override
        public boolean accepts(ProcessContext context) {
            boolean accepted = false;
            for(Processor processor: processors){
                accepted = accepted || processor.accepts(context);
            }
            return accepted;
        }
    }
    
    @BeforeEach
    void setUp() {
        chain = new TestChain();
        p1 = mock(Processor.class);
        p2 = mock(Processor.class);
        p3 = mock(Processor.class);
        context = mock(ProcessContext.class);
    }
    
    @Test
    void testAddToStartAndEnd() {
        chain.addToEnd(p2).addToStart(p1).addToEnd(p3);
        List<Processor> processors = chain.getProcessors();
        assertEquals(List.of(p1, p2, p3), processors);
    }
    
    @Test
    void testAddBeforeAndAfter() {
        chain.addToEnd(p1).addToEnd(p3);
        chain.addBefore(p3, p2);
        List<Processor> processors = chain.getProcessors();
        assertEquals(List.of(p1, p2, p3), processors);
        Processor<ProcessContext> p4 = mock(Processor.class);
        chain.addAfter(p2, p4);
        processors = chain.getProcessors();
        assertEquals(List.of(p1, p2, p4, p3), processors);
    }
    
    @Test
    void testRemove() {
        chain.addToEnd(p1).addToEnd(p2);
        chain.remove(p1);
        List<Processor> processors = chain.getProcessors();
        assertEquals(List.of(p2), processors);
    }
    
    @Test
    void testProcessCallsAcceptingProcessors() {
        chain.addToEnd(p1).addToEnd(p2);
        when(p1.accepts(context)).thenReturn(true);
        when(p2.accepts(context)).thenReturn(false);
        chain.process(context);
        verify(p1).beforeProcess(context);
        verify(p1).process(context);
        verify(p1).afterProcess(context);
        verify(p2, never()).process(context);
    }
    
    @Test
    void testAccepts() {
        chain.addToEnd(p1).addToEnd(p2);
        when(p1.accepts(context)).thenReturn(false);
        when(p2.accepts(context)).thenReturn(true);
        assertTrue(chain.accepts(context));
        when(p2.accepts(context)).thenReturn(false);
        assertFalse(chain.accepts(context));
    }
}