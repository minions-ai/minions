package com.minionsai.claude.workflow.task;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class Task {

  private String taskId;
  private LocalDateTime creationTime;
  private String description;
  private Map<String, Object> parameters;
  private String type;


}
