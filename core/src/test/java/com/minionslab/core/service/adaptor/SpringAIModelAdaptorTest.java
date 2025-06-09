package com.minionslab.core.service.adaptor;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.model.ModelCall;
import com.minionslab.core.model.ChatModelRepository;
import com.minionslab.core.model.ModelCallResponse;
import com.minionslab.core.common.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringAIModelAdaptorTest {
    private ChatModelRepository chatModelRepository;
    private SpringAIModelAdaptor adaptor;
    private ChatModel chatModel;

    @BeforeEach
    void setUp() {
        chatModelRepository = mock(ChatModelRepository.class);
        chatModel = mock(ChatModel.class);
        adaptor = new SpringAIModelAdaptor(chatModelRepository);
    }

    @Test
    void testAcceptsReturnsFalseForNullConfig() {
        ModelCall call = mock(ModelCall.class);
        when(call.getModelConfig()).thenReturn(null);
        assertFalse(adaptor.accepts(call));
    }

    @Test
    void testAcceptsHandlesProviderLogic() {
        ModelConfig config = mock(ModelConfig.class);
        when(config.getModelId()).thenReturn("any");
        when(config.getProvider()).thenReturn("OPENAI");
        ModelCall call = mock(ModelCall.class);
        when(call.getModelConfig()).thenReturn(config);
        when(chatModelRepository.getChatModel(anyString(), anyString())).thenReturn(chatModel);
        assertTrue(adaptor.accepts(call));
    }

    @Test
    void testProcessSetsResponse() {
        // Arrange
        ModelConfig config = mock(ModelConfig.class);
        when(config.getProvider()).thenReturn("openai");
        when(config.getModelId()).thenReturn("gpt-4o");
        ModelCall.ModelCallRequest request = new ModelCall.ModelCallRequest(Collections.emptyList(), Collections.emptyMap(), null);
        ModelCall call = mock(ModelCall.class);
        when(call.getModelConfig()).thenReturn(config);
        when(call.getRequest()).thenReturn(request);
        doNothing().when(call).setResponse(any());
        when(chatModelRepository.getChatModel(anyString(), anyString())).thenReturn(chatModel);

        // Mock ChatResponse and its getResults
        ChatResponse chatResponse = mock(ChatResponse.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResults()).thenReturn(Collections.emptyList());

        // Act
        ModelCall result = adaptor.process(call);

        // Assert
        verify(chatModelRepository).getChatModel("openai", "gpt-4o");
        verify(chatModel).call(any(Prompt.class));
        verify(call).setResponse(any(ModelCallResponse.class));
        assertEquals(call, result);
    }
} 