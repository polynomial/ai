package com.cyster.insight.impl.assistants;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.assistant.ManagedAssistantService;
import com.cyster.insight.service.assistant.ManagedAssistant;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.openai.OpenAiFactory;

// Currently a Scenario creates an Conversation, should create an Assistant, then this would be used
// an Assistant would return a Conversation

@Component
public class CodingAssistant implements ManagedAssistant {
    private final String CODING_ASSISTANT = "code";
      
    public CodingAssistant(OpenAiFactory openAiFactory, ManagedAssistantService assistantService) {
    }
    
    @Override
    public String getName() {
        return CODING_ASSISTANT;
    }

    @Override
    public Conversation start() {
        throw new RuntimeException("not implemented");
    }

}
