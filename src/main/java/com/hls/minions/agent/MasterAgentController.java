package com.hls.minions.agent;

import com.hls.minions.agent.MasterAgentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class MasterAgentController {
    private final MasterAgentService masterAgentService;

    public MasterAgentController(MasterAgentService masterAgentService) {
        this.masterAgentService = masterAgentService;
    }

    @PostMapping("/process")
    public String processPrompt(@RequestBody PromptRequest request) {
        return masterAgentService.processUserPrompt(request.prompt());
    }

    public static record PromptRequest (String prompt){

    }
}


