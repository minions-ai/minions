package com.minionslab.core.memory.strategy.persistence.postgres;

import com.minionslab.core.common.message.*;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostgresMessageMapperTest {
    @Test
    void testToSqlParamsAndBack() throws Exception {
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
        
        PostgresMessageMapper mapper = new PostgresMessageMapper();
        Object[] params = mapper.toSqlParams(msg);
        assertEquals("1", params[0]);
        assertTrue(params[2].toString().contains("testEntity"));
        assertEquals("Hello world", params[3]);
        assertEquals("USER", params[4]);
        assertEquals("AGENT", params[5]);
        assertEquals(5, params[6]);
        assertTrue(params[7].toString().contains("1.0"));
    }

    @Test
    void testMapRow() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("id")).thenReturn("1");
        when(rs.getString("timestamp")).thenReturn(Instant.now().toString());
        when(rs.getString("metadata")).thenReturn("{\"entityType\":\"testEntity\"}");
        when(rs.getString("content")).thenReturn("Test");
        when(rs.getString("role")).thenReturn("USER");
        when(rs.getString("scope")).thenReturn("AGENT");
        when(rs.getInt("token_count")).thenReturn(5);
        when(rs.getString("embedding")).thenReturn("[1.0,2.0]");

        PostgresMessageMapper mapper = new PostgresMessageMapper();
        Message msg = mapper.mapRow(rs, 0);
        assertEquals("1", msg.getId());
        assertEquals("Test", msg.getContent());
        assertEquals(MessageRole.USER, msg.getRole());
        assertEquals(MessageScope.AGENT, msg.getScope());
        assertEquals(5, msg.getTokenCount());
        assertNotNull(msg.getMetadata());
        assertTrue(msg.getMetadata().containsKey("entityType"));

    }
} 