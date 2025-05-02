package com.minionslab.mcp.agent;

import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.step.MCPStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for MCP agents.
 */
@Data
@Accessors(chain = true)
@Builder
@Document(collection = "agent_recipes")
public class AgentRecipe {
    
    @Id
    private String id;
    @NotBlank
    private String systemPrompt;
    @NotNull
    private ModelConfig modelConfig;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    @Builder.Default
    private Map<String, Object> constraints = new HashMap<>();
    private SecurityPolicy securityPolicy;
    @Builder.Default
    private List<MCPStep> steps = new ArrayList<>();
    private List<String> requiredTools;
    
    @Builder.Default
    private ChatMemoryRepository memoryRepository = new InMemoryChatMemoryRepository();
}