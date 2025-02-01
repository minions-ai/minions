package com.hls.minions.agent;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterAgent {

    @Autowired
    private ChatClient.Builder chatClientBuilder;
    private final String prompt;


    public MasterAgent() {


        prompt = ResourceUtils.getText("classpath:master_agent.txt");
    }


    public String processPrompt(String userRequest) {
        ChatClient chatClient = chatClientBuilder
                .defaultFunctions("communicationAgent"
                        , "followUpQuestionAgent"
                        , "policyVerificationTool"
                        , "coverageVerificationAgent"
                        , "damageAssessmentAgent"
                        , "fraudDetectionAgent")
                .build();

        String response = chatClient.prompt().system(prompt).user(userRequest).call().content();

        log.info("Response from the LLM {}", response);
        return response;
    }
}
