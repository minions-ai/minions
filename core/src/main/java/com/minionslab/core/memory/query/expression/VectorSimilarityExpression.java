package com.minionslab.core.memory.query.expression;

import com.minionslab.core.common.message.Message;

public class VectorSimilarityExpression implements MemoryQueryExpression {
    private final float[] embedding;
    private final int topK;
    
    public VectorSimilarityExpression(float[] embedding, int topK) {
        this.embedding = embedding;
        this.topK = topK;
    }
    
    public boolean evaluate(Message message) {
        // Placeholder: in real usage, similarity search is external
        return true;
    }
    

}
