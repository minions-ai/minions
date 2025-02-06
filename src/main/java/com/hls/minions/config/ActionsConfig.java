package com.hls.minions.config;

import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
public class ActionsConfig {

    @Bean
    public Action<String, String> startAgentsAction() {
        return context -> {
            context.getTarget().getEntryActions().forEach(action -> {
                action.apply(context);
            });
        };
    }
}
