package com.minionslab.core.memory.strategy.persistence.mongo;

import com.minionslab.core.memory.query.MemoryQuery;
import com.minionslab.core.memory.query.expression.*;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MongoQueryTranslatorTest {
    private final MongoQueryTranslator translator = new MongoQueryTranslator();

    @Test
    void testTranslateFieldEquals() {
        var expr = Expr.eq("foo", "bar");
        var query = MemoryQuery.builder().expression(expr).build();
        Query mongoQuery = translator.translate(query);
        Document doc = mongoQuery.getQueryObject();
        assertEquals("bar", doc.get("foo"));
    }

    @Test
    void testTranslateContainsKeyword() {
        var expr = Expr.contains("text", "world");
        var query = MemoryQuery.builder().expression(expr).build();
        Query mongoQuery = translator.translate(query);
        Document doc = mongoQuery.getQueryObject();

        assertTrue(doc.get("text").toString().contains("world"));
    }

    @Test
    void testTranslateRangeAfterBefore() {
        Instant now = Instant.now();
        var afterExpr = Expr.after("ts", now);
        var beforeExpr = Expr.before("ts", now);
        var afterQuery = MemoryQuery.builder().expression(afterExpr).build();
        var beforeQuery = MemoryQuery.builder().expression(beforeExpr).build();
        Document afterDoc = translator.translate(afterQuery).getQueryObject();
        Document beforeDoc = translator.translate(beforeQuery).getQueryObject();
        assertTrue(((Document) afterDoc.get("ts")).containsKey("$gt"));
        assertTrue(((Document) beforeDoc.get("ts")).containsKey("$lt"));
    }

    @Test
    void testTranslateMetadataMatch() {
        var expr = Expr.metadata("foo", 42);
        var query = MemoryQuery.builder().expression(expr).build();
        Document doc = translator.translate(query).getQueryObject();
        assertEquals(42, doc.get("metadata.foo"));
    }

    @Test
    void testTranslateLogicalAndOrNot() {
        var e1 = Expr.eq("a", 1);
        var e2 = Expr.eq("b", 2);
        var andExpr = Expr.and(e1, e2);
        var orExpr = Expr.or(e1, e2);
        var notExpr = Expr.not(e1);
        Document andDoc = translator.translate(MemoryQuery.builder().expression(andExpr).build()).getQueryObject();
        Document orDoc = translator.translate(MemoryQuery.builder().expression(orExpr).build()).getQueryObject();
        Document notDoc = translator.translate(MemoryQuery.builder().expression(notExpr).build()).getQueryObject();
        assertTrue(andDoc.containsKey("$and"));
        assertTrue(orDoc.containsKey("$or"));
        assertTrue(notDoc.containsKey("$nor"));
    }

    @Test
    void testTranslateWithLimit() {
        var expr = Expr.eq("foo", "bar");
        var query = MemoryQuery.builder().expression(expr).limit(5).build();
        Query mongoQuery = translator.translate(query);
        assertEquals(5, mongoQuery.getLimit());
    }
} 