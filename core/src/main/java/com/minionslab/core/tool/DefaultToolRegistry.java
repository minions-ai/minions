package com.minionslab.core.tool;


import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.api.ResponseFormat.JsonSchema;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultToolRegistry implements ToolRegistry {
    
    private final Map<String, ToolCallback> registeredTools;
    

    @Autowired(required = false)
    private final List<McpSyncClient> mcpSyncClients;
    
    @Autowired(required = false)
    private final List<McpAsyncClient> mcpAsyncClients;
    
    public DefaultToolRegistry(List<McpSyncClient> mcpSyncClients, List<McpAsyncClient> mcpAsyncClients) {
        this.mcpSyncClients = mcpSyncClients;
        this.mcpAsyncClients = mcpAsyncClients;
        this.registeredTools = new ConcurrentHashMap<>();
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
    public List<ToolCallback> getTools(List<String> toolNames) {
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        
        for (String requiredTool : toolNames) {
            toolCallbacks.add(getTool(requiredTool));
        }
        return toolCallbacks;
    }
    
    public ToolCallback getTool(String toolName) {
        String lowerCaseToolName = getInternalToolName(toolName);
        if (!isToolAvailable(lowerCaseToolName)) {
            throw new ToolException.ToolNotAvailableException(String.format("Tool %s is not available", toolName));
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