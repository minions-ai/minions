package com.minionslab.agents.smarthome;

import com.minionslab.core.agent.AgentRecipe;
import com.minionslab.core.agent.AgentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.minionslab")
public class SmartHomeAgentApp implements CommandLineRunner {
    
    private final AgentRecipe agentRecipe;
    private final AgentService agentService;
    
    public SmartHomeAgentApp(AgentRecipe agentRecipe, AgentService agentService) {
        this.agentRecipe = agentRecipe;
        this.agentService = agentService;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SmartHomeAgentApp.class, args);
  
    }
    
    
    @Override
    public void run(String... args) throws Exception {
        
        agentService.runAgent(agentRecipe);
        
    }
}
