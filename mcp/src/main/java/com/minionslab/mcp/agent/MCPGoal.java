package com.minionslab.mcp.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * Represents a goal that an MCP agent aims to achieve.
 */
@Data
@Accessors(chain = true)
@Builder
public class MCPGoal {
    
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    @NotBlank
    private String description;
    
    @Builder.Default
    private GoalStatus status = GoalStatus.PENDING;
    
    @Builder.Default
    private List<MCPGoal> subgoals = new ArrayList<>();
    
    @Builder.Default
    private Map<String, Object> successCriteria = new HashMap<>();
    private Date deadline;
    private int priority;
    
    
    public boolean isAchieved() {
        if (status != GoalStatus.COMPLETED) {
            return false;
        }
        
        // Check if all subgoals are achieved
        for (MCPGoal subgoal : subgoals) {
            if (!subgoal.isAchieved()) {
                return false;
            }
        }
        
        return true;
    }
} 