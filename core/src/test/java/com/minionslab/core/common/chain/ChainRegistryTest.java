package com.minionslab.core.common.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChainRegistryTest {
    private ChainRegistry registry;
    private Chain<Processor,ProcessContext> chain;
    private ProcessContext context;

    @BeforeEach
    void setUp() {
        registry = new ChainRegistry(null,null);
        chain = mock(Chain.class);
        context = mock(ProcessContext.class);
    }

    @Test
    void testRegisterAndProcess() {
        when(chain.accepts(context)).thenReturn(true);
        when(chain.process(context)).thenReturn(context);
        registry.register("test", chain);
        assertTrue(registry.canProcess(context));
        assertEquals(context, registry.process(context));
    }

    @Test
    void testUnregisterChainByName() {
        registry.register("test", chain);
        registry.unregisterChain("test");
        assertThrows(IllegalArgumentException.class, () -> registry.process(context));
    }

    @Test
    void testUnregisterChainByInstance() {
        registry.register("test", chain);
        registry.unregisterChain(chain);
        assertThrows(IllegalArgumentException.class, () -> registry.process(context));
    }

    @Test
    void testProcessThrowsIfNoChainAccepts() {
        assertThrows(IllegalArgumentException.class, () -> registry.process(context));
    }
} 