package com.minionslab.core.memory.strategy.persistence.mongo; // Example package

import com.minionslab.core.message.Message;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;
import com.minionslab.core.message.SimpleMessage;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component // To be injectable
public class MessageMongoMapper {
    /**
     * Converts a domain {@link Message} object into a {@link Document}
     * suitable for storing in MongoDB.
     *
     * @param message The domain Message object.
     * @return A BSON Document representing the message.
     */
    public Document toDocument(Message message) {
        Document doc = new Document();
        doc.put("id", message.getId());
        doc.put("timestamp", message.getTimestamp() != null ? message.getTimestamp().toString() : null);
        doc.put("metadata", message.getMetadata() != null ? new Document(message.getMetadata()) : new Document());
        doc.put("content", message.getContent());
        doc.put("role", message.getRole() != null ? message.getRole().name() : null);
        doc.put("scope", message.getScope() != null ? message.getScope().name() : null);
        doc.put("tokenCount", message.getTokenCount());
        float[] embeddingArr;
        try {
            embeddingArr = (float[]) message.getClass().getMethod("getEmbedding").invoke(message);
        } catch (Exception ignored) {
            embeddingArr = null;
        }
        if (embeddingArr != null) {
            final float[] embFinal = embeddingArr;
            List<Float> embList = IntStream.range(0, embFinal.length)
                .mapToObj(i -> embFinal[i])
                .collect(Collectors.toList());
            doc.put("embedding", embList);
        }
        return doc;
    }

    /**
     * Converts a MongoDB {@link Document} to a domain {@link Message}.
     * Convenience method that delegates to the Map-based toDomain.
     *
     * @param document The BSON Document from MongoDB.
     * @return A domain Message object.
     */
    public Message toDomain(Document document) {
        SimpleMessage.SimpleMessageBuilder msgBuilder = SimpleMessage.builder();
        msgBuilder.id(document.getString("id"));
        String ts = document.getString("timestamp");
        if (ts != null) msgBuilder.timestamp(Instant.parse(ts));
        Object metaObj = document.get("metadata");
        if (metaObj instanceof Map) {
            msgBuilder.metadata(new HashMap<>((Map<String, Object>) metaObj));
        } else {
            msgBuilder.metadata(new HashMap<>());
        }
        msgBuilder.content(document.getString("content"));
        String roleStr = document.getString("role");
        if (roleStr != null) msgBuilder.role(MessageRole.valueOf(roleStr));
        String scopeStr = document.getString("scope");
        if (scopeStr != null) msgBuilder.scope(MessageScope.valueOf(scopeStr));
        msgBuilder.tokenCount(document.getInteger("tokenCount", 0));
        List<?> embList = document.getList("embedding", Object.class);
/*        if (embList != null) {
            float[] emb = new float[embList.size()];
            for (int i = 0; i < embList.size(); i++) {
                emb[i] = ((Number) embList.get(i)).floatValue();
            }
            msgBuilder.(emb);
        }*/
        return msgBuilder.build();
    }
}