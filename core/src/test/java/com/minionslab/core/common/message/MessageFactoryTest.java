package com.minionslab.core.common.message;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.MessageFactory;
import com.minionslab.core.common.message.MessageRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageFactoryTest {
    private MessageFactory factory;
    private DefaultResourceLoader resourceLoader;
    private Resource resource;

    @BeforeEach
    void setUp() {
        resourceLoader = mock(DefaultResourceLoader.class);
        resource = mock(Resource.class);
        factory = new MessageFactory(resourceLoader);
    }

    @Test
    void testCreateMessageFromContent() {
        Message msg = factory.createMessageFromContent("hello", MessageRole.USER);
        assertEquals("hello", msg.getContent());
        assertEquals(MessageRole.USER, msg.getRole());
    }

    @Test
    void testCreateMessageFromResourceSuccess() throws IOException {
        ResourceLoader loader = mock(DefaultResourceLoader.class);
        MessageFactory realFactory = new MessageFactory((DefaultResourceLoader) loader);
        String content = "test content";
        Resource res = mock(Resource.class);
        when(loader.getResource(anyString())).thenReturn(res);
        when(res.exists()).thenReturn(true);
        when(res.isReadable()).thenReturn(true);
        when(res.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        Message msg = realFactory.createMessageFromResource("dummy", MessageRole.SYSTEM);
        assertEquals(content, msg.getContent());
    }

    @Test
    void testCreateMessageFromResourceNotFound() {
        MessageFactory realFactory = new MessageFactory(resourceLoader);
        ResourceLoader loader = mock(ResourceLoader.class);
        Resource res = mock(Resource.class);
        when(loader.getResource(anyString())).thenReturn(res);
        when(res.exists()).thenReturn(false);
        assertThrows(RuntimeException.class, () -> realFactory.createMessageFromResource("notfound", MessageRole.SYSTEM));
    }

    @Test
    void testCreateMessageFromResourceUnreadable() {
        MessageFactory realFactory = new MessageFactory(resourceLoader);
        ResourceLoader loader = mock(ResourceLoader.class);
        Resource res = mock(Resource.class);
        when(loader.getResource(anyString())).thenReturn(res);
        when(res.exists()).thenReturn(true);
        when(res.isReadable()).thenReturn(false);
        assertThrows(RuntimeException.class, () -> realFactory.createMessageFromResource("unreadable", MessageRole.SYSTEM));
    }

    @Test
    void testCreateMessageFromResourceIOException() throws IOException {
        MessageFactory realFactory = new MessageFactory(resourceLoader);
        ResourceLoader loader = mock(ResourceLoader.class);
        Resource res = mock(Resource.class);
        when(loader.getResource(anyString())).thenReturn(res);
        when(res.exists()).thenReturn(true);
        when(res.isReadable()).thenReturn(true);
        when(res.getInputStream()).thenThrow(new IOException("fail"));
        assertThrows(RuntimeException.class, () -> realFactory.createMessageFromResource("ioerror", MessageRole.SYSTEM));
    }
} 