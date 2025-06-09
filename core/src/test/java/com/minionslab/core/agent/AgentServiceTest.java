package com.minionslab.core.agent;

import com.minionslab.core.common.chain.ChainRegistry;
import com.minionslab.core.common.chain.ProcessContext;
import com.minionslab.core.common.chain.ProcessResult;
import com.minionslab.core.memory.MemoryFactory;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.service.ModelCallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AgentService}.
 * <p>
 * Scenarios:
 * <ul>
 *   <li>Run agent by ID fetches recipe and delegates to runAgent</li>
 * </ul>
 * <p>
 * Setup: Mocks AgentRecipeRepository, ModelCallService, ChainRegistry, MemoryFactory, AgentRecipe, and Message.
 */
class AgentServiceTest {
    private AgentRecipeRepository recipeRepository;
    private ModelCallService modelCallService;
    private ChainRegistry chainRegistry;
    private MemoryFactory memoryFactory;
    private AgentService service;
    private AgentRecipe recipe;
    private Message message;

    /**
     * Sets up the test environment before each test.
     * Mocks dependencies and initializes AgentService.
     * Expected: AgentService is ready for use in each test.
     */
    @BeforeEach
    void setUp() {
        recipeRepository = mock(AgentRecipeRepository.class);
        modelCallService = mock(ModelCallService.class);
        chainRegistry = mock(ChainRegistry.class);
        memoryFactory = mock(MemoryFactory.class);
        service = new AgentService(recipeRepository, modelCallService, chainRegistry, memoryFactory);
        recipe = mock(AgentRecipe.class);
        message = mock(Message.class);
        MessageBundle messageBundle = mock(MessageBundle.class);
        when(messageBundle.getMessagesByRole()).thenReturn(new java.util.HashMap<>());
        when(recipe.getMessageBundle()).thenReturn(messageBundle);
    }

    /**
     * Tests that runAgentById fetches the recipe and delegates to runAgent.
     * Setup: Mocks recipeRepository to return a recipe for a given ID.
     * Expected: runAgent is called with the recipe and the correct context is returned.
     */
    @Test
    void testRunAgentByIdFetchesRecipeAndDelegates() {
        when(recipeRepository.findById("id")).thenReturn(recipe);
        AgentService spyService = spy(service);
        AgentContext context = mock(AgentContext.class);
        doReturn(context).when(spyService).runAgent(recipe);
        assertEquals(context, spyService.runAgent("id"));
        verify(recipeRepository).findById("id");
        verify(spyService).runAgent(recipe);
    }

    @Test
    void testRunAgentByIdThrowsIfNotFound() {
        when(recipeRepository.findById("id")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.runAgent("id"));
    }

    @Test
    void testRunAgentRecipeDelegatesToRunAgentRecipeMessage() {
        AgentService spyService = spy(service);
        AgentContext context = mock(AgentContext.class);
        doReturn(context).when(spyService).runAgent(eq(recipe), any(Message.class));
        assertEquals(context, spyService.runAgent(recipe));
        verify(spyService).runAgent(eq(recipe), any(Message.class));
    }

    @Test
    void testRunAgentRecipeMessageCreatesAgentAndCallsChainRegistry() {
        // Create a mock AgentResult that also implements ProcessContext
        ProcessResult agentResult = mock(ProcessResult.class, withSettings().extraInterfaces(ProcessContext.class));
        AgentContext context = mock(AgentContext.class);
        AgentService spyService = spy(service);
        doReturn(context).when(spyService).createAgentContext(any());
        when(chainRegistry.process(any(ProcessContext.class))).thenReturn(context);
        assertEquals(context, service.runAgent(recipe, message));
        verify(chainRegistry).process(any(ProcessContext.class));
    }

    @Test
    void testRunAgentRecipeStringDelegatesToRunAgentRecipeMessage() {
        AgentService spyService = spy(service);
        AgentContext context = mock(AgentContext.class);

        doReturn(context).when(spyService).createAgentContext(any());
        doReturn(context).when(spyService).runAgent(eq(recipe), any(Message.class));
        assertEquals(context, spyService.runAgent(recipe, "msg"));
        verify(spyService).runAgent(eq(recipe), any(Message.class));
    }
} 