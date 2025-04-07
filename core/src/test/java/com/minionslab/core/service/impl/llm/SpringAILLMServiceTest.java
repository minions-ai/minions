package com.minionslab.core.service.impl.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.domain.PromptComponent;
import com.minionslab.core.domain.enums.PromptType;
import com.minionslab.core.exception.LLMServiceException;
import com.minionslab.core.service.impl.llm.model.LLMRequest;
import com.minionslab.core.service.impl.llm.model.LLMResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpringAILLMServiceTest {

    private SpringAILLMService springAILLMService;

    @Mock
    private MinionPrompt minionPrompt;

    @BeforeEach
    void setUp() {
        springAILLMService = new SpringAILLMService();
    }

    @Test
    @DisplayName("Should process request with system and user components")
    void processRequest_WithSystemAndUserComponents_ShouldReturnResponse() {
        // Arrange
        String promptId = UUID.randomUUID().toString();
        String promptVersion = "1.0";
        String systemPromptText = "You are a helpful assistant";
        String userPromptText = "Please help me with {request}";
        String userRequest = "my question";

        Map<PromptType, PromptComponent> components = new HashMap<>();
        components.put(PromptType.SYSTEM, PromptComponent.builder()
                .type(PromptType.SYSTEM)
                .text(systemPromptText)
                .build());
        components.put(PromptType.USER_TEMPLATE, PromptComponent.builder()
                .type(PromptType.USER_TEMPLATE)
                .text(userPromptText)
                .build());

        when(minionPrompt.getId()).thenReturn(promptId);
        when(minionPrompt.getVersion()).thenReturn(promptVersion);
        when(minionPrompt.getComponents()).thenReturn(components);

        LLMRequest request = LLMRequest.builder()
                .prompt(minionPrompt)
                .userRequest(userRequest)
                .build();

        // Act
        LLMResponse response = springAILLMService.processRequest(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRequestId());
        assertEquals(promptId, response.getPromptId());
        assertEquals(promptVersion, response.getPromptVersion());
        assertNotNull(response.getTimestamp());
        assertEquals("System: " + systemPromptText + "\nUser: " + userRequest + "\nResponse: This is a mock response from the LLM service.", 
                response.getResponseText());
    }

    @Test
    @DisplayName("Should process request with only system component")
    void processRequest_WithOnlySystemComponent_ShouldReturnResponse() {
        // Arrange
        String promptId = UUID.randomUUID().toString();
        String promptVersion = "1.0";
        String systemPromptText = "You are a helpful assistant";
        String userRequest = "my question";

        Map<PromptType, PromptComponent> components = new HashMap<>();
        components.put(PromptType.SYSTEM, PromptComponent.builder()
                .type(PromptType.SYSTEM)
                .text(systemPromptText)
                .build());

        when(minionPrompt.getId()).thenReturn(promptId);
        when(minionPrompt.getVersion()).thenReturn(promptVersion);
        when(minionPrompt.getComponents()).thenReturn(components);

        LLMRequest request = LLMRequest.builder()
                .prompt(minionPrompt)
                .userRequest(userRequest)
                .build();

        // Act
        LLMResponse response = springAILLMService.processRequest(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRequestId());
        assertEquals(promptId, response.getPromptId());
        assertEquals(promptVersion, response.getPromptVersion());
        assertNotNull(response.getTimestamp());
        assertEquals("System: " + systemPromptText + "\nUser: " + userRequest + "\nResponse: This is a mock response from the LLM service.", 
                response.getResponseText());
    }

    @Test
    @DisplayName("Should throw exception when prompt is null")
    void processRequest_WithNullPrompt_ShouldThrowException() {
        // Arrange
        LLMRequest request = LLMRequest.builder()
                .prompt(null)
                .userRequest("my question")
                .build();

        // Act & Assert
        assertThrows(LLMServiceException.class, () -> springAILLMService.processRequest(request));
    }

    @Test
    @DisplayName("Should throw exception when components are null")
    void processRequest_WithNullComponents_ShouldThrowException() {
        // Arrange
        when(minionPrompt.getComponents()).thenReturn(null);

        LLMRequest request = LLMRequest.builder()
                .prompt(minionPrompt)
                .userRequest("my question")
                .build();

        // Act & Assert
        assertThrows(LLMServiceException.class, () -> springAILLMService.processRequest(request));
    }

    @Test
    @DisplayName("Should throw exception when system component is missing")
    void processRequest_WithMissingSystemComponent_ShouldThrowException() {
        // Arrange
        Map<PromptType, PromptComponent> components = new HashMap<>();
        components.put(PromptType.USER_TEMPLATE, PromptComponent.builder()
                .type(PromptType.USER_TEMPLATE)
                .text("Please help me with {request}")
                .build());

        when(minionPrompt.getComponents()).thenReturn(components);

        LLMRequest request = LLMRequest.builder()
                .prompt(minionPrompt)
                .userRequest("my question")
                .build();

        // Act & Assert
        assertThrows(LLMServiceException.class, () -> springAILLMService.processRequest(request));
    }
} 