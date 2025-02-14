package com.hls.minions.patient.agent;

import com.hls.minions.core.agent.MasterAgentPrompt;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PatientMasterAgentPrompt extends MasterAgentPrompt {

  @Override public String systemPrompt() {
    Resource system_prompt = new DefaultResourceLoader().getResource("classpath:agents/patient/master_agent.txt");
    try {
      String prompt = system_prompt.getContentAsString(Charset.defaultCharset());
      return prompt;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return "";
  }

}
