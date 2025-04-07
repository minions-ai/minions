package com.minionslab.core.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring implementation of MinionEventPublisher that uses ApplicationEventPublisher.
 */
@Component
@RequiredArgsConstructor
public class SpringMinionEventPublisher implements MinionEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publishEvent(MinionEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
} 