package com.minionslab.mcp.tool;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.openai.api.ResponseFormat.JsonSchema;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultMCPToolOrchestrator implements MCPToolOrchestrator {
    
    private final Map<String, ToolCallback> registeredTools;
    
/*    @Autowired
    private SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;
    
    @Autowired
    private AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider;*/
    
    @Autowired
    private final List<McpSyncClient> mcpSyncClients;
    
/*    @Autowired
    private final List<McpAsyncClient> mcpAsyncClients;*/
    
    public DefaultMCPToolOrchestrator(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
//        this.mcpAsyncClients = mcpAsyncClients;
        this.registeredTools = new ConcurrentHashMap<>();
        registerStepCompletedTool();
        initializeToolsCache();
    }
    
    
    private void initializeToolsCache() {
        SyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        if (syncMcpToolCallbackProvider != null) {
            for (ToolCallback toolCallback : syncMcpToolCallbackProvider.getToolCallbacks()) {
                registerTool(toolCallback);
            }
        }
        
    }
    

    
    @Override
    public void registerTool(ToolCallback toolCallback) {
        registeredTools.put(toolCallback.getToolDefinition().name(), toolCallback);
    }
    
    @Override
    public Map<String, Object> getToolParameters(String toolName) {
        if (toolName == null) {
            throw new IllegalArgumentException("Tool name cannot be null");
        }
        ToolCallback tool = registeredTools.get(toolName.toLowerCase());
        if (tool == null) {
            throw new IllegalArgumentException("Tool '" + toolName + "' is not registered");
        }
        JsonSchema inputSchema = JsonSchema.builder().schema(tool.getToolDefinition().inputSchema()).build();
        return inputSchema.getSchema();
    }
    
    private boolean isParameterRequired(Object paramSpec) {
        if (paramSpec instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> spec = (Map<String, Object>) paramSpec;
            return Boolean.TRUE.equals(spec.get("required"));
        }
        return false;
    }
    
    @Override
    public List<ToolDefinition> getAvailableTools() {
        return registeredTools.values().stream()
                              .map(ToolCallback::getToolDefinition)
                              .toList();
    }
    
    @Override
    public List<ToolCallback> getTools(List<String> requiredTools) {
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        
        for (String requiredTool : requiredTools) {
            toolCallbacks.add(getTool(requiredTool));
        }
        return toolCallbacks;
    }
    
    public ToolCallback getTool(String requiredTool) {
        String lowerCaseToolName = getInternalToolName(requiredTool);
        if (!isToolAvailable(lowerCaseToolName)) {
            throw new ToolException.ToolNotAvailableException(String.format("Tool %s is not available", requiredTool));
        }
        for (String s : registeredTools.keySet()) {
            if (s.contains(lowerCaseToolName)) {
                return registeredTools.get(s);
            }
        }
        return null;
    }
    
    private static @NotNull String getInternalToolName(String requiredTool) {
        return requiredTool.toLowerCase().replace("-", "_");
    }
    
    @Override
    public boolean isToolAvailable(String toolName) {
        if (toolName == null) {
            return false;
        }
        
        return registeredTools.keySet().stream().anyMatch(name -> name.contains(toolName));
    }
    
}