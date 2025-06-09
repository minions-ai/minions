package com.minionslab.core.model;

import com.minionslab.core.common.message.EntityMessage;
import com.minionslab.core.common.message.Message;
import com.minionslab.core.step.StepCompletionOutputInstructions.StepCompletionInstruction;
import com.minionslab.core.tool.ToolCall;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@RequiredArgsConstructor
/**
 * <b>Extensibility:</b>
 * <ul>
 *   <li>Extend ModelCallResponse to add custom fields, metadata, or result handling logic.</li>
 *   <li>Override methods to support advanced message, tool call, or entity extraction.</li>
 * </ul>
 * <b>Usage:</b> ModelCallResponse represents the output of a model call, including messages, tool calls, and completion instructions.
 */
public class ModelCallResponse {
    private final List<Message> messages;
    private final List<ToolCall> toolCalls;
    private StepCompletionInstruction completionInstruction;
    private List<EntityMessage> entities;
    

    
    public List<Message> getMessages() {
        return messages;
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
    
    public List<EntityMessage> getEntities() {return entities;}

    public StepCompletionInstruction getCompletionInstruction() {
        return completionInstruction;
    }

    public void setCompletionInstruction(StepCompletionInstruction completionInstruction) {
        this.completionInstruction = completionInstruction;
    }
} 