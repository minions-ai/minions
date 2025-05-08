package com.minionslab.mcp.service;

import com.minionslab.mcp.config.ModelConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatModelServiceIntegrationTest {
    
    @Autowired
    private ChatModelService chatModelService;
    
    @Mock
    private ChatModel chatModel;
    
    @BeforeEach
    void setUp() {
    
    }
    
    @Test
    void testGetModelReturnsChatModel() {
        ModelConfig config = ModelConfig.builder()
                                        .modelId("chat")
                                        .provider("OpenAI")
                                        .version("1.0")
                                        .maxContextLength(1024)
                                        .build();
        
        Object model = chatModelService.getModel(config);
        assertNotNull(model);
        assertTrue(model.toString().toLowerCase().contains("openaichatmodel"));
    }
    
    @Test
    void testGetModelThrowsForUnknownModel() {
        ModelConfig config = ModelConfig.builder()
                                        .modelId("unknown-model")
                                        .provider("unknown")
                                        .version("1.0")
                                        .maxContextLength(1024)
                                        .build();
        
        assertThrows(IllegalArgumentException.class, () -> chatModelService.getModel(config));
    }
} 