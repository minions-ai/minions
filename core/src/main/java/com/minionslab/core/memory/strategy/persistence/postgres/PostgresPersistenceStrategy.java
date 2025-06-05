package com.minionslab.core.memory.strategy.persistence.postgres;

import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.strategy.MemoryItem;
import com.minionslab.core.memory.strategy.MemoryPersistenceStrategy;
import com.minionslab.core.message.Message;
import com.minionslab.core.message.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PostgresPersistenceStrategy implements MemoryPersistenceStrategy<Message> {
    private static final Logger log = LoggerFactory.getLogger(PostgresPersistenceStrategy.class);
    private final JdbcTemplate jdbcTemplate;
    private final PostgresQueryTranslator queryConverter;
    private final PostgresMessageMapper messageMapper;
    private static final String MESSAGES_TABLE = "messages";

    @Autowired
    public PostgresPersistenceStrategy(JdbcTemplate jdbcTemplate,
                                       PostgresQueryTranslator queryConverter,
                                       PostgresMessageMapper messageMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryConverter = queryConverter;
        this.messageMapper = messageMapper;
    }

    @Override
    public <T extends MemoryItem> T save(T item) {
        if (item == null) throw new IllegalArgumentException("MemoryItem to save cannot be null.");
        if (item instanceof Message) {
            Message msg = (Message) item;
            String sql = "INSERT INTO " + MESSAGES_TABLE + " (id, timestamp, metadata, content, role, scope, token_count, embedding) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT (id) DO UPDATE SET timestamp = EXCLUDED.timestamp, metadata = EXCLUDED.metadata, content = EXCLUDED.content, role = EXCLUDED.role, scope = EXCLUDED.scope, token_count = EXCLUDED.token_count, embedding = EXCLUDED.embedding";
            Object[] params = messageMapper.toSqlParams(msg);
            jdbcTemplate.update(sql, params);
            return item;
        }
        throw new UnsupportedOperationException("Save not implemented for type: " + item.getClass().getName());
    }

    @Override
    public <T extends MemoryItem> List<T> saveAll(List<T> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream().map(this::save).collect(Collectors.toList());
    }

    @Override
    public <T extends MemoryItem> Optional<T> findById(String id, Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            String sql = "SELECT * FROM " + MESSAGES_TABLE + " WHERE id = ?";
            List<Message> results = jdbcTemplate.query(sql, messageMapper, id);
            if (results.isEmpty()) return Optional.empty();
            @SuppressWarnings("unchecked")
            T result = (T) results.get(0);
            return Optional.of(result);
        }
        return Optional.empty();
    }

    @Override
    public <T extends MemoryItem> boolean deleteById(String id, Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            String sql = "DELETE FROM " + MESSAGES_TABLE + " WHERE id = ?";
            return jdbcTemplate.update(sql, id) > 0;
        }
        return false;
    }

    @Override
    public <T extends MemoryItem> void deleteAllOfType(Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            String sql = "DELETE FROM " + MESSAGES_TABLE;
            jdbcTemplate.update(sql);
        }
    }

    @Override
    public <T extends MemoryItem> long count(Class<T> itemType) {
        if (Message.class.isAssignableFrom(itemType) || SimpleMessage.class.isAssignableFrom(itemType)) {
            String sql = "SELECT COUNT(*) FROM " + MESSAGES_TABLE;
            return jdbcTemplate.queryForObject(sql, Long.class);
        }
        return 0;
    }

    @Override
    public List<Message> fetchCandidateMessages(MemoryQuery memoryQuery) {
        String sql = queryConverter.translate(memoryQuery.getExpression()).toString();
        return jdbcTemplate.query(sql, messageMapper);
    }
} 