package com.minionslab.core.memory.strategy.persistence.inmemory;

import com.minionslab.core.common.message.Message;
import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.MessageScope;
import com.minionslab.core.common.message.SimpleMessage;
import com.minionslab.core.memory.query.MemoryQuery;
import static com.minionslab.core.memory.query.expression.Expr.*;

import com.minionslab.core.memory.query.expression.Expr;
import com.minionslab.core.memory.query.expression.ExprUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPersistenceStrategyTest {
    private InMemoryPersistenceStrategy strategy;
    private SimpleMessage msg1, msg2;
    
    @BeforeEach
    void setUp() {
        strategy = new InMemoryPersistenceStrategy();
        msg1 = SimpleMessage.builder()
                            .id("1")
                            .conversationId("C1")
                            .content("Hello world")
                            .metadata(Map.of())
                            .role(MessageRole.USER)
                            .scope(MessageScope.AGENT)
                            .timestamp(Instant.now())
                            .metadata(Map.of("entityType", "testEntity")).build();
        
        
        SimpleMessage.SimpleMessageBuilder msg2Builder = SimpleMessage.builder();
        msg2Builder.id("2");
        msg2Builder.content("Goodbye world");
        msg2Builder.role(MessageRole.ASSISTANT);
        msg2Builder.scope(MessageScope.AGENT);
        msg2Builder.timestamp(Instant.now());
        msg2Builder.metadata(Map.of("entityType", "testEntity"));
        msg2 = msg2Builder.build();
    }
    
    @Test
    void testSaveAndFindById() {
        strategy.save(msg1);
        Optional<Message> found = strategy.findById("1", Message.class);
        assertTrue(found.isPresent());
        assertEquals("Hello world", found.get().getContent());
    }
    
    @Test
    void testSaveAllAndCount() {
        strategy.saveAll(List.of(msg1, msg2));
        assertEquals(2, strategy.count(Message.class));
    }
    
    @Test
    void testDeleteById() {
        strategy.save(msg1);
        assertTrue(strategy.deleteById("1", Message.class));
        assertFalse(strategy.findById("1", Message.class).isPresent());
    }
    
    @Test
    void testDeleteAllOfType() {
        strategy.saveAll(List.of(msg1, msg2));
        strategy.deleteAllOfType(Message.class);
        assertEquals(0, strategy.count(Message.class));
    }
    
    @Test
    void testFetchCandidateMessages() {
        strategy.saveAll(List.of(msg1, msg2));
        MemoryQuery query = MemoryQuery.builder()
                                       .expression(ExprUtil.getUserMessagesExpression("C1").and(contains("content", "Hello"),metadata("entityType", "testEntity")))
                                       .limit(10)
                                       .build();
        List<Message> results = strategy.fetchCandidateMessages(query);
        assertEquals(1, results.size());
        assertEquals("1", results.get(0).getId());
    }
}