package com.minionslab.core.agent;

import com.minionslab.core.config.ModelConfig;
import com.minionslab.core.step.DefaultStep;
import com.minionslab.core.step.DefaultStepGraph;
import com.minionslab.core.step.Step;
import com.minionslab.core.model.MessageBundle;
import com.minionslab.core.message.DefaultMessage;
import com.minionslab.core.message.MessageRole;
import com.minionslab.core.message.MessageScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TravelPlannerAgentRecipeFactory {
    
    
    public static AgentRecipe create() {
        // Model config for the agent
        ModelConfig modelConfig = ModelConfig.builder()
                                             .modelId("chat")
                                             .provider("openai")
                                             .version("2024-06")
                                             .temperature(0.7)
                                             .maxTokens(1024)
                                             .build();
        
        // Step 1: Gather Requirements
/*        Step gatherRequirements = new SimpleModelStep(
                "gather-requirements",
                "Gather user travel preferences",
                Set.of(),
                "What are your travel preferences? (destination, dates, budget, interests)"
        );*/
        
        // Step 2: Search Destinations (simulate tool call)
        MessageBundle searchDestBundle = new MessageBundle();
        searchDestBundle.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("Search for destinations matching preferences")
                .build());
        Step searchDestinations = new DefaultStep(
                "search-destinations",
                searchDestBundle,
                Set.of("destinationSearchTool", "packagedTravelTool")
        );
        
        // Step 3: Summarize Plan
        MessageBundle summarizePlanBundle = new MessageBundle();
        summarizePlanBundle.addMessage(DefaultMessage.builder()
                .role(MessageRole.SYSTEM)
                .scope(MessageScope.STEP)
                .content("Summarize the best travel plan for the user based on the search results.")
                .build());
        Step summarizePlan = new DefaultStep(
                "summarize-plan",
                summarizePlanBundle,
                Set.of("destinationSearchTool", "packagedTravelTool")
        );
        
        // Step graph: gather-requirements -> search-destinations -> summarize-plan
        Map<String, List<String>> stepGraph = Map.of(
//            "gather-requirements", List.of("search-destinations"),
                "search-destinations", List.of("summarize-plan"),
                "summarize-plan", List.of() // end
                                                    );
        
        return AgentRecipe.builder()
                          .id("travel-planner-agent")
                          .systemPrompt("You are a helpful travel planning assistant.")
                          .modelConfig(modelConfig)
                          .stepGraph(new DefaultStepGraph(List.of(searchDestinations, summarizePlan), stepGraph))
                          .requiredTools(List.of("destinationSearchTool", "packagedTravelSearch"))
                          .build();
    }
    
}