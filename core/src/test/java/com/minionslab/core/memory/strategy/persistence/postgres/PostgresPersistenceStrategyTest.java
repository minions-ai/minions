package com.minionslab.core.memory.strategy.persistence.postgres;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.MessageScope;
import com.minionslab.core.common.message.SimpleMessage;
import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.memory.query.expression.ExprUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PostgresPersistenceStrategyTest.Config.class)
class PostgresPersistenceStrategyTest {
    @MockBean
    private JdbcTemplate jdbcTemplate;
    @MockBean
    private PostgresQueryTranslator queryConverter;
    @MockBean
    private PostgresMessageMapper messageMapper;
    
    @Autowired
    private PostgresPersistenceStrategy strategy;
    
    private SimpleMessage msg1, msg2;
    
    @BeforeEach
    void setUp() {
        msg1 = SimpleMessage.builder()
                            .id("1")
                            .conversationId("C1")
                            .content("Hello world")
                            .role(MessageRole.USER)
                            .scope(MessageScope.AGENT)
                            .timestamp(Instant.now())
                            .metadata(Map.of("entityType", "testEntity"))
                            .build();
        
        msg2 = SimpleMessage.builder()
                            .id("2")
                            .conversationId("C1")
                            .content("Goodbye world")
                            .role(MessageRole.ASSISTANT)
                            .scope(MessageScope.AGENT)
                            .timestamp(Instant.now())
                            .metadata(Map.of("entityType", "testEntity"))
                            .build();
    }
    
    @Test
    void testSaveAndFindById() {
        when(messageMapper.toSqlParams(msg1)).thenReturn(new Object[]{"1", null, null, "Hello world", "USER", "AGENT", 0, null});
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.query(anyString(), eq(messageMapper), eq("1"))).thenReturn(List.of(msg1));
        
        strategy.save(msg1);
        Optional<Message> found = strategy.findById("1", Message.class);
        assertTrue(found.isPresent());
        assertEquals("Hello world", found.get().getContent());
        verify(jdbcTemplate, atLeastOnce()).update(anyString(), any(Object[].class));
        verify(jdbcTemplate, atLeastOnce()).query(anyString(), eq(messageMapper), eq("1"));
    }
    
    @Test
    void testSaveAllAndCount() {
        when(messageMapper.toSqlParams(any(SimpleMessage.class))).thenReturn(new Object[]{"id", null, null, "content", "USER", "AGENT", 0, null});
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(2L);
        
        strategy.saveAll(List.of(msg1, msg2));
        assertEquals(2, strategy.count(Message.class));
        verify(jdbcTemplate, atLeastOnce()).update(anyString(), any(Object[].class));
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class));
    }
    
    @Test
    void testDeleteById() {
        when(jdbcTemplate.update(anyString(), any(Object.class))).thenReturn(1);
        when(jdbcTemplate.query(anyString(), eq(messageMapper), eq("1"))).thenReturn(List.of());
        
        strategy.save(msg1);
        assertTrue(strategy.deleteById("1", Message.class));
        assertFalse(strategy.findById("1", Message.class).isPresent());
        verify(jdbcTemplate, atLeastOnce()).update(anyString(), any(Object.class));
    }
    
    @Test
    void testDeleteAllOfType() {
        when(jdbcTemplate.update(anyString())).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(0L);
        
        strategy.saveAll(List.of(msg1, msg2));
        strategy.deleteAllOfType(Message.class);
        assertEquals(0, strategy.count(Message.class));
        verify(jdbcTemplate, atLeastOnce()).update(anyString());
        verify(jdbcTemplate, atLeastOnce()).queryForObject(anyString(), eq(Long.class));
    }
    
    @Test
    void testFetchCandidateMessages() {
        MemoryQuery query = MemoryQuery.builder()
                                       .expression(ExprUtil.getUserMessagesExpression("C1").and(Expr.contains("content", "hello"), Expr.eq("entityType", "testEntity"))).build();
        

        PostgresQueryTranslator.SqlQuery sqlQuery = new PostgresQueryTranslator.SqlQuery("TRUE", List.of());
        when(queryConverter.translate(any())).thenReturn(sqlQuery);
        when(jdbcTemplate.query(anyString(), eq(messageMapper))).thenReturn(List.of(msg1));
        
        List<Message> results = strategy.fetchCandidateMessages(query);
        assertTrue(results.stream().anyMatch(m -> m.getId().equals("1")));
        verify(queryConverter).translate(any());
        verify(jdbcTemplate).query(anyString(), eq(messageMapper));
    }
    
    @Configuration
    static class Config {
        @Bean
        public PostgresPersistenceStrategy strategy(JdbcTemplate jdbcTemplate, PostgresQueryTranslator queryConverter, PostgresMessageMapper messageMapper) {
            return new PostgresPersistenceStrategy(jdbcTemplate, queryConverter, messageMapper);
        }
    }
} 