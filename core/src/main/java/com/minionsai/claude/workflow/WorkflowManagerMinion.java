package com.minionsai.claude.workflow;

import com.minionsai.claude.agent.Minion;
import lombok.experimental.SuperBuilder;
import org.springframework.ai.model.function.FunctionCallback;

@SuperBuilder
public class WorkflowManagerMinion extends Minion {

  @Override protected FunctionCallback[] getAvailableTools() {
    return new FunctionCallback[0];
  }

  @Override protected String getPromptFilePath() {
    return "";
  }
}