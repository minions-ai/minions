package com.minionslab.core.common.message;

import com.minionslab.core.common.message.EmbeddingMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmbeddingMessageTest {
    @Test
    void testConstructionAndGetters() {
        float[] embedding = new float[] {1.0f, 2.0f, 3.0f};
        EmbeddingMessage msg = EmbeddingMessage.builder().embedding(embedding).build();
        msg.setEmbedding(embedding);
        assertArrayEquals(embedding, msg.getEmbedding());
    }
} 