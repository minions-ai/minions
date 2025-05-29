package com.minionslab.core.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import com.minionslab.core.memory.strategy.MemoryStrategyRegistry;

class MemoryFactoryTest {
    private MemoryFactory factory;
    private MemoryStrategyRegistry strategyRegistry;
    private MemoryDefinitionRegistry defRegistry;

    @BeforeEach
    void setUp() {
        strategyRegistry = mock(MemoryStrategyRegistry.class);
        defRegistry = mock(MemoryDefinitionRegistry.class);
        factory = new MemoryFactory(strategyRegistry, defRegistry);
    }

    @Test
    void testCreateMemoriesThrowsOnUnknownDefinition() {
        when(defRegistry.getMemoryDefinition("foo")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> factory.createMemories(List.of("foo")));
    }
} 