package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExprTest {
    @Test
    void testEqExpression() {
        Message msg = mock(Message.class);
        when(msg.getFieldValue("foo")).thenReturn("bar");
        var expr = Expr.eq("foo", "bar");
        assertTrue(expr.evaluate(msg));
        assertEquals(new FieldEqualsExpression("foo", "bar"), expr);
    }

    @Test
    void testContainsExpression() {
        Message msg = mock(Message.class);
        when(msg.getFieldValue("text")).thenReturn("hello world");
        var expr = Expr.contains("text", "world");
        assertTrue(expr.evaluate(msg));
    }

    @Test
    void testAfterBeforeExpression() {
        Message msg = mock(Message.class);
        Instant now = Instant.now();
        Instant before = now.minusSeconds(10);
        Instant after = now.plusSeconds(10);
        when(msg.getFieldValue("ts")).thenReturn(now);
        var afterExpr = Expr.after("ts", before);
        var beforeExpr = Expr.before("ts", after);
        assertTrue(afterExpr.evaluate(msg));
        assertTrue(beforeExpr.evaluate(msg));
    }

    @Test
    void testMetadataExpression() {
        Message msg = mock(Message.class);
        when(msg.getMetadata()).thenReturn(Map.of("k", 42));
        var expr = Expr.metadata("k", 42);
        assertTrue(expr.evaluate(msg));
    }

    @Test
    void testVectorExpression() {
        Message msg = mock(Message.class);
        var expr = Expr.vector(new float[]{1,2,3}, 5);
        assertTrue(expr.evaluate(msg)); // always true in placeholder
    }

    @Test
    void testAndOrNotExpressions() {
        Message msg = mock(Message.class);
        when(msg.getFieldValue("a")).thenReturn(1);
        when(msg.getFieldValue("b")).thenReturn(2);
        var eqA = Expr.eq("a", 1);
        var eqB = Expr.eq("b", 2);
        var andExpr = Expr.and(eqA, eqB);
        var orExpr = Expr.or(eqA, Expr.eq("b", 3));
        var notExpr = Expr.not(Expr.eq("a", 2));
        assertTrue(andExpr.evaluate(msg));
        assertTrue(orExpr.evaluate(msg));
        assertTrue(notExpr.evaluate(msg));
    }

    @Test
    void testLogicalExpressionEqualsHashCode() {
        var e1 = Expr.eq("x", 1);
        var e2 = Expr.eq("y", 2);
        var and1 = Expr.and(e1, e2);
        var and2 = Expr.and(e1, e2);
        assertEquals(and1, and2);
        assertEquals(and1.hashCode(), and2.hashCode());
    }
} 