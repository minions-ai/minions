package com.minionslab.core.common.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChainRegistry}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Register, process, and unregister chains by name and instance</li>
 *   <li>Throws exception if no chain accepts the context</li>
 * </ul>
 * <p>
 * Setup: Mocks Chain and ProcessContext. Initializes ChainRegistry with mock ObjectProviders and a chain map.
 */
class ChainRegistryTest {
    private ChainRegistry registry;
    private Chain<Processor, ProcessContext> chain;
    private ProcessContext context;
    
    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and initializes ChainRegistry.
     * Expected: ChainRegistry is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        chain = mock(Chain.class);
        registry = new ChainRegistry(mock(ObjectProvider.class), mock(ObjectProvider.class), Map.of("chain", chain));
        
        context = mock(ProcessContext.class);
    }
    
    /**
     * Tests that register and process work as expected.
     * Setup: Chain accepts the context and returns it.
     * Expected: process returns the context.
     */
    @Test
    void testRegisterAndProcess() {
        when(chain.accepts(context)).thenReturn(true);
        when(chain.process(context)).thenReturn(context);
        
        registry.register("test", chain);
        assertTrue(registry.canProcess(context));
        assertEquals(context, registry.process(context));
    }
    
    /**
     * Tests that unregisterChain by name removes the chain.
     * Setup: Registers a chain and then unregisters by name.
     * Expected: process throws IllegalArgumentException after unregister.
     */
    @Test
    void testUnregisterChainByName() {
        registry.register("test", chain);
        registry.unregisterChain("test");
        assertThrows(IllegalArgumentException.class, () -> registry.process(context));
    }
    
    /**
     * Tests that unregisterChain by instance removes the chain.
     * Setup: Registers a chain and then unregisters by instance.
     * Expected: process throws IllegalArgumentException after unregister.
     */
    @Test
    void testUnregisterChainByInstance() {
        registry.register("test", chain);
        registry.unregisterChain(chain);
        assertThrows(IllegalArgumentException.class, () -> registry.process(context));
    }
    
    /**
     * Tests that process throws if no chain accepts the context.
     * Setup: No chain accepts the context.
     * Expected: process throws IllegalArgumentException.
     */
    @Test
    void testProcessThrowsIfNoChainAccepts() {
        assertThrows(IllegalArgumentException.class, () -> registry.process(context));
    }
} 