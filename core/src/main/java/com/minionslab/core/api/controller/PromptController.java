package com.minionslab.core.api.controller;

import com.minionslab.core.api.dto.PromptRequest;
import com.minionslab.core.domain.MinionPrompt;
import com.minionslab.core.service.PromptService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {
    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping
    public MinionPrompt createPrompt(@Valid @RequestBody PromptRequest request) {
        MinionPrompt prompt = request.toMinionPrompt();
        return promptService.updateComponents(prompt.getId(), request.getComponents());
    }

    @PutMapping("/{id}")
    public MinionPrompt updatePrompt(
            @PathVariable String id,
            @Valid @RequestBody PromptRequest request) {
        return promptService.updateComponents(id, request.getComponents());
    }

    @PatchMapping("/{id}/content")
    public MinionPrompt addContent(
            @PathVariable String id,
            @RequestBody String content) {
        return promptService.addContent(id, content);
    }
} 