package com.minionsai.core.service;

import com.minionsai.core.openai.Tool;
import com.minionsai.core.openai.ToolParameters;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.function.DefaultFunctionCallbackResolver;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FunctionCallService {

  //  private final Map<String, Class> functionClassMap = new ConcurrentHashMap<>();
  private final FunctionCallbackResolver callbackResolver;
  private final ApplicationContext applicationContext;


  public FunctionCallService(FunctionCallbackResolver callbackResolver, ApplicationContext applicationContext) {
    this.callbackResolver = callbackResolver;
    this.applicationContext = applicationContext;

  }


  @PostConstruct
  private void init() {
    List<String> functions = getFunctions();
//    functionClassMap.putAll(functions);
  }

  private List<String> getFunctions() {
    // Load the Spring parameters using ToolConfiguration

    return
        List.of(applicationContext.getBeanNamesForType(Function.class));

  }


  public Tool getTool(String functionName) throws IOException {
    DefaultFunctionCallbackResolver defaultFunctionCallbackResolver = new DefaultFunctionCallbackResolver();
    defaultFunctionCallbackResolver.setApplicationContext(applicationContext);
    FunctionCallback resolve = defaultFunctionCallbackResolver.resolve(functionName);
    String description = resolve.getDescription();
    String inputTypeSchema = resolve.getInputTypeSchema();
    ToolParameters toolParameters = ToolParameters.fromJson(inputTypeSchema);
    Tool tool = Tool.builder().name(functionName).description(description).build();
    return tool;
  }


  public List<Tool> getTools() {
    List<Tool> tools = new ArrayList<>();
    for (String s : getFunctions()) {
      try {
        tools.add(getTool(s));
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
    return tools;
  }


  public String call(String name, String argumentsJson) {
    FunctionCallback resolve = callbackResolver.resolve(name);
    String call = "";
    try {
      call = resolve.call(argumentsJson);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
    }
    return call;
  }


}
