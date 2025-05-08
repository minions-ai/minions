package com.minionslab.mcp.message;

import com.minionslab.mcp.tool.MCPToolCall;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.*;


@Data
@Accessors(chain = true)
@Builder
public class DefaultMCPMessage implements MCPMessage {
    
    private MessageRole role;
    private String content;
    @Builder.Default
    private Instant timestamp = Instant.now();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private List<MCPToolCall.MCPToolCallRequest> toolCalls = new ArrayList<>();
    private String modelId;
    private int tokenCount;
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    

}
