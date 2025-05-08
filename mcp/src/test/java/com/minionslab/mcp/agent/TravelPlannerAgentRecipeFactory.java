package com.minionslab.mcp.agent;

import com.minionslab.mcp.config.ModelConfig;
import com.minionslab.mcp.step.DefaultStep;
import com.minionslab.mcp.step.Step;

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
        Step searchDestinations = new DefaultStep(
                "search-destinations",
                "Search for destinations matching preferences",
                Set.of("destinationSearchTool","packagedTravelTool"),
                "Find destinations that match the user's preferences using the destinationSearchTool."
        );
        
        // Step 3: Summarize Plan
        Step summarizePlan = new DefaultStep(
                "summarize-plan",
                "Summarize the best travel plan",
                Set.of("destinationSearchTool","packagedTravelTool"),
                "Summarize the best travel plan for the user based on the search results."
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
                          .steps(List.of(searchDestinations, summarizePlan))
                          .stepGraph(stepGraph)
                          .requiredTools(List.of("destinationSearchTool","packagedTravelSearch"))
                          .build();
    }
    
}