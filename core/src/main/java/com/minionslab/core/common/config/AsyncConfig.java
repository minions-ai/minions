package com.minionslab.core.common.config;

import com.minionslab.core.context.MinionContext;
import com.minionslab.core.context.MinionContextHolder;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

  private final MinionContextHolder contextHolder;

  public AsyncConfig(MinionContextHolder contextHolder) {
    this.contextHolder = contextHolder;
  }

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(task -> {
      MinionContext contextSnapshot = contextHolder.getContext();
      return () -> {
        MinionContext originalContext = contextHolder.getContext();
        try {
          contextHolder.setContext(contextSnapshot);
          task.run();
        } finally {
          contextHolder.setContext(originalContext);
        }
      };
    });
    executor.initialize();
    return executor;
  }
}
