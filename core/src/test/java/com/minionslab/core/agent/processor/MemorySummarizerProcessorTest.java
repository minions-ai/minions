package com.minionslab.core.agent.processor;

import com.minionslab.core.agent.AgentContext;
import com.minionslab.core.agent.SummarizationConfig;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.memory.MemoryManager;
import com.minionslab.core.memory.MemorySubsystem;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ModelCallResponse;
import com.minionslab.core.service.ModelCallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.minionslab.core.agent.AgentConfig;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemorySummarizerProcessorTest {
    
    @Mock
    private AgentContext agentContext;
    @Mock
    private MemoryManager memoryManager;
    @Mock
    private ModelCallService modelCallService;
    @Mock
    private SummarizationConfig summarizationConfig;
    @Mock
    private ModelConfig modelConfig;
    @Mock
    private Message message1;
    @Mock
    private Message message2;
    @Mock
    private ModelCall modelCall;
    @Mock
    private ModelCallResponse modelCallResponse;

    private MemorySummarizerProcessor processor;
    @Mock
    private AgentConfig agentConfig;
    private MockedStatic<AgentContext> mockedStaticAgentContext;
    
    @BeforeEach
    void setUp() {
        mockedStaticAgentContext = mockStatic(AgentContext.class);
        processor = new MemorySummarizerProcessor(modelCallService);
        
        // Setup context mocks
        when(agentContext.getMemoryManager()).thenReturn(memoryManager);
        when(agentContext.getConversationId()).thenReturn("testConversationId");
        
        // Setup static config mocks
        mockedStaticAgentContext.when(AgentContext::getConfig).thenReturn(agentConfig);
        when(agentConfig.getSummarizationConfig()).thenReturn(summarizationConfig);
        when(summarizationConfig.getInputMessageLimit()).thenReturn(10);
        when(summarizationConfig.getModelConfig()).thenReturn(modelConfig);
        
        // Setup model call mocks
        when(modelCall.getResponse()).thenReturn(modelCallResponse);
        when(modelCallResponse.getMessages()).thenReturn(new ArrayList<>());
    }
    
    @AfterEach
    void tearDown() {
        mockedStaticAgentContext.close();
    }
    
    @Test
    void testDoProcessThrowsExceptionIfMemoryManagerIsNull() {
        when(agentContext.getMemoryManager()).thenReturn(null);
        assertThrows(MemoryNotAvailableException.class, () -> processor.doProcess(agentContext));
    }
    
    @Test
    void testDoProcessQueriesMemoryAndStoresMessages() throws Exception {
        List<Message> memoryMessages = new ArrayList<>();
        memoryMessages.add(message1);
        memoryMessages.add(message2);
        when(memoryManager.query(any(MemoryQuery.class))).thenReturn(memoryMessages);
        when(modelCallService.call(any(ModelCall.class))).thenReturn(modelCall);
        
        AgentContext agentContext1 = processor.process(agentContext);
        
        verify(memoryManager).query(any(MemoryQuery.class));
        verify(modelCallService).call(any(ModelCall.class));
        verify(memoryManager).storeAll(anyList(), eq(MemorySubsystem.EPISODIC));
        verify(memoryManager).storeAll(anyList(), eq(MemorySubsystem.VECTOR));

    }
} 