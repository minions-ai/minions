package com.minionslab.core.common.message;


import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.function.Supplier;

@Data
@Accessors
@SuperBuilder
public class EmbeddingMessage extends SimpleMessage {
    
    private float[] embedding;
    
    
    @Override
    protected Map<String, Supplier<Object>> populateFieldAccessors() {
        Map<String, Supplier<Object>> supplierMap = super.populateFieldAccessors();
        supplierMap.put("embedding", this::getEmbedding);
        return supplierMap;
    }
}
