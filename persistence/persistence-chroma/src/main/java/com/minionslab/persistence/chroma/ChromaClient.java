package com.minionslab.persistence.chroma;

import com.minionslab.core.message.Message;
import java.util.List;


public interface ChromaClient {
    void save(Message message, float[] embedding);
    List<Message> vectorSearch(float[] embedding, int topK);
    List<Message> findAll();
} 