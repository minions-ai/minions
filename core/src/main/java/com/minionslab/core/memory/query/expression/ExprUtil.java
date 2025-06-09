package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.MessageRole;
import com.minionslab.core.common.message.MessageScope;

public class ExprUtil {
    
    public static MemoryQueryExpression getAssistantMessagesExpression(String conversationId) {
        return getConversationIdExpression(conversationId)
                       .and(Expr.eq("role", MessageRole.ASSISTANT));
    }
    
    public static MemoryQueryExpression getConversationIdExpression(String conversationId) {
        return Expr.eq("conversationId", conversationId);
    }
    
    public static MemoryQueryExpression getUserMessagesExpression(String conversationId) {
        return getConversationIdExpression(conversationId)
                       .and(Expr.eq("role", MessageRole.USER));
    }
    
    public static MemoryQueryExpression getScopedMessagesExpression(MessageScope scope) {
        return Expr.eq("scope", scope);
    }
}