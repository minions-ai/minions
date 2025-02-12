package com.example.multiagent.tools;

public interface Tool {

  String getToolId();

  String getDescription();

  // Execute the tool with the given input.
  Object execute(Object input);
}
