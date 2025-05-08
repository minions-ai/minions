package com.minionslab.mcp.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestTravelAgent {
    
    @Autowired
    private AgentService agentService;
    
    @BeforeEach
    void setUp() {
    
    }
    
    @Test
    public void testTravelAgent() {
        AgentRecipe agentRecipe = TravelPlannerAgentRecipeFactory.create();
        
        AgentResult agentResult = agentService.runAgent(agentRecipe, "I want to travel to Spain between June and July, my budget is $10000, and I like to do sight seeings");
        
        System.out.println(agentResult);
    }
}
