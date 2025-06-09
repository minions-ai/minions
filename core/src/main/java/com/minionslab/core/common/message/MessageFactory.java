package com.minionslab.core.common.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class MessageFactory {

    private final ResourceLoader resourceLoader;
    
    public MessageFactory(DefaultResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    public Message createMessageFromContent(String content, MessageRole role) {
        return SimpleMessage.builder()
                            .id(generateMessageId())
                            .role(role)
                            .content(content)
                            .build();
    }
    
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
    
    public Message createMessageFromResource(String resourceLocation, MessageRole role) {
        return createMessageFromResource(resourceLocation, role, StandardCharsets.UTF_8);
    }

    public Message createMessageFromResource(String resourceLocation, MessageRole role, Charset charset) {
        Resource resource = resourceLoader.getResource(resourceLocation);
        if (!resource.exists()) {
            String msg = "Resource does not exist: " + resourceLocation;
            log.error(msg);
            throw new RuntimeException(msg);
        }
        if (!resource.isReadable()) {
            String msg = "Resource is not readable: " + resourceLocation;
            log.error(msg);
            throw new RuntimeException(msg);
        }
        try {
            String content = readResourceContent(resource, charset);
            if (content == null || content.isBlank()) {
                String msg = "Resource is empty: " + resourceLocation;
                log.warn(msg);
            }
            return createMessageFromContent(content, role);
        } catch (IOException e) {
            String msg = "Failed to read resource: " + resourceLocation;
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private String readResourceContent(Resource resource, Charset charset) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, charset);
        }
    }
}
