package com.minionslab.core.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import com.minionslab.core.memory.strategy.MemoryStrategyRegistry;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Unit tests for {@link MemoryFactory}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Throws exception on unknown memory definition</li>
 * </ul>
 * <p>
 * Setup: Mocks MemoryStrategyRegistry and MemoryDefinitionRegistry. Initializes MemoryFactory.
 */
class MemoryFactoryTest {
    private MemoryFactory factory;
    private MemoryStrategyRegistry strategyRegistry;
    private MemoryDefinitionRegistry defRegistry;

    @BeforeEach
    void setUp() {
        strategyRegistry = mock(MemoryStrategyRegistry.class);
        defRegistry = mock(MemoryDefinitionRegistry.class);
        factory = new MemoryFactory(strategyRegistry, defRegistry,mock(ObjectProvider.class));
    }

    /**
     * Tests that createMemories throws IllegalArgumentException for unknown memory definition.
     * Setup: MemoryDefinitionRegistry returns null for a given definition.
     * Expected: createMemories throws IllegalArgumentException.
     */
    @Test
    void testCreateMemoriesThrowsOnUnknownDefinition() {
        when(defRegistry.getMemoryDefinition(any())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> factory.createMemories(List.of(MemorySubsystem.ENTITY)));
    }
} 