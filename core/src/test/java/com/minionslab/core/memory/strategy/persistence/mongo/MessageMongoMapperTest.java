package com.minionslab.core.memory.strategy.persistence.mongo;

import com.minionslab.core.message.EmbeddingMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.message.SimpleMessage;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageMongoMapperTest {
//    @Test
    void testToDocumentAndToDomain() {
        EmbeddingMessage.EmbeddingMessageBuilder msgBuilder = EmbeddingMessage.builder();
        msgBuilder.id("1");
        msgBuilder.content("Hello world");
        msgBuilder.role(MessageRole.USER);
        msgBuilder.scope(MessageScope.AGENT);
        msgBuilder.timestamp(Instant.now());
        msgBuilder.tokenCount(5);
        msgBuilder.metadata(Map.of("entityType", "testEntity"));
        msgBuilder.embedding(new float[]{1.0F});
        SimpleMessage msg = msgBuilder.build();
        
        
        MessageMongoMapper mapper = new MessageMongoMapper();
        Document doc = mapper.toDocument(msg);
        assertEquals("1", doc.getString("id"));
        assertEquals("Hello world", doc.getString("content"));
        assertEquals("USER", doc.getString("role"));
        assertEquals("AGENT", doc.getString("scope"));
        assertEquals(5, doc.getInteger("tokenCount"));
        assertNotNull(doc.get("metadata"));
        assertNotNull(doc.get("embedding"));
        
        SimpleMessage roundTrip = (SimpleMessage) mapper.toDomain(doc);
        assertEquals(msg.getId(), roundTrip.getId());
        assertEquals(msg.getContent(), roundTrip.getContent());
        assertEquals(msg.getRole(), roundTrip.getRole());
        assertEquals(msg.getScope(), roundTrip.getScope());
        assertEquals(msg.getTokenCount(), roundTrip.getTokenCount());
        assertEquals(msg.getMetadata().get("entityType"), roundTrip.getMetadata().get("entityType"));
        
    }
} 