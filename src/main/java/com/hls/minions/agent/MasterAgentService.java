package com.hls.minions.agent;

import com.hls.minions.agent.MasterAgent;
import org.springframework.stereotype.Service;

@Service
public class MasterAgentService {
    private final MasterAgent masterAgent;

    public MasterAgentService(MasterAgent masterAgent) {
        this.masterAgent = masterAgent;
    }

    public String processUserPrompt(String prompt) {
        return masterAgent.processPrompt(prompt);
    }
}
