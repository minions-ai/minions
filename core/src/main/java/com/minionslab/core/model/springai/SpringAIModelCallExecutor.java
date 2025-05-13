package com.minionslab.core.model.springai;

import com.minionslab.core.context.AgentContext;
import com.minionslab.core.model.*;
import com.minionslab.core.tool.impl.DestinationSearchTool;
import com.minionslab.core.tool.impl.PackagedTravelToolClient;
import com.minionslab.core.util.MessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class SpringAIModelCallExecutor extends AbstractModelCallExecutor<ChatResponse> {
    private final Executor executor;
    private final ModelCallExecutionContext modelCallContext;
    private final ChatModel chatModel;

    public SpringAIModelCallExecutor(ModelCall modelCall, AgentContext context, ChatModel chatModel) {
        this(modelCall, context, chatModel, ForkJoinPool.commonPool());
    }

    public SpringAIModelCallExecutor(ModelCall modelCall, AgentContext context, ChatModel chatModel, Executor executor) {
        super(modelCall, context);
        this.executor = executor;
        this.chatModel = chatModel;
        ToolCallingChatOptions chatOptions = (ToolCallingChatOptions) context.getModelConfig().getParameters().getOrDefault(
                "chatOptions", ToolCallingChatOptions.builder().build());
        this.modelCallContext = ModelCallExecutionContext.builder()
                .chatModel(chatModel)
                .chatMemory(null)
                .conversationId(context.getConversationid())
                .chatOptions(chatOptions)
                .messageConverter(new MessageConverter())
                .build();
    }

    @Override
    protected ChatResponse callModel(Prompt prompt) {
        org.springframework.ai.chat.prompt.Prompt springPrompt = toSpringPrompt(prompt);
        return chatModel.call(springPrompt);
    }

    @Override
    protected void handleProviderResponse(ChatResponse rawResponse) {
        modelMemory.saveChatResponse(modelCallContext.conversationId(), rawResponse);
    }

    @Override
    protected ModelCallResponse toMCPModelCallResponse(ChatResponse rawResponse) {
        return new ModelCallResponse(rawResponse);
    }

    @Override
    protected Executor getExecutor() {
        return executor;
    }

    private org.springframework.ai.chat.prompt.Prompt toSpringPrompt(Prompt prompt) {
        List<Message> springMessages = MessageConverter.toSpringMessages(prompt.getMessages());
        ToolCallingChatOptions chatOptions = modelCallContext.chatOptions().copy();
        chatOptions.setToolCallbacks(List.of(
                PackagedTravelToolClient.getcallback(),
                DestinationSearchTool.getcallback()
        ));
        return new org.springframework.ai.chat.prompt.Prompt(springMessages, chatOptions);
    }
} 