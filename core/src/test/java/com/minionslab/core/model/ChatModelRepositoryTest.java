package com.minionslab.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChatModelRepositoryTest {
    private ApplicationContext applicationContext;
    private ChatModelRepository repository;
    private ChatModel chatModel;
    
    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        chatModel = mock(ChatModel.class);
        repository = new ChatModelRepository(applicationContext);
    }
    
    @Test
    void testGetChatModelByBeanNameMatch() {
        Map<String, ChatModel> beans = new HashMap<>();
        beans.put("openai-gpt-4o", chatModel);
        when(applicationContext.getBeansOfType(ChatModel.class)).thenReturn(beans);
        ChatModel result = repository.getChatModel("openai", "gpt-4o");
        assertSame(chatModel, result);
    }
    
    @Test
    void testGetChatModelByClassNameMatch() {
        Map<String, ChatModel> beans = new HashMap<>();
        // Simulate a class name match using a spy
        OpenAiChatModel modelWithClass = mock(OpenAiChatModel.class, withSettings().extraInterfaces(ChatModel.class));
        
        ChatModel customModel = mock(OpenAiChatModel.class);
        beans.put("irrelevant", customModel);
        when(applicationContext.getBeansOfType(ChatModel.class)).thenReturn(beans);
        ChatModel result = repository.getChatModel("openai", "chat");
        assertSame(customModel, result);
    }
    
    @Test
    void testGetChatModelThrowsIfNotFound() {
        when(applicationContext.getBeansOfType(ChatModel.class)).thenReturn(new HashMap<>());
        assertThrows(IllegalArgumentException.class, () -> repository.getChatModel("foo", "bar"));
    }
} 