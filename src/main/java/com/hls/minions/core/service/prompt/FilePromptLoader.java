package com.hls.minions.core.service.prompt;

import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FilePromptLoader {

  public String getPrompt(String scopeKey, String promptId) throws IOException {
    DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
    Resource resource = defaultResourceLoader.getResource("Classpath:prompts/" + scopeKey + "/" + promptId + ".txt");
    return resource.getContentAsString(Charset.defaultCharset());
  }
}
