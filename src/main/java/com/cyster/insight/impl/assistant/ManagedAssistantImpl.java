package com.cyster.insight.impl.assistant;

import com.cyster.insight.service.assistant.ManagedAssistant;
import com.cyster.insight.service.conversation.Conversation;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;

public class ManagedAssistantImpl implements ManagedAssistant {
    private OpenAiService openAiService;
    private Assistant assistant;
    
    public ManagedAssistantImpl(OpenAiService openAiService, Assistant assistant) {
        this.openAiService = openAiService;
        this.assistant = assistant;
    }
    
    public String getId() {
        return this.assistant.getId();
    }
    
    public String getName() {
        return this.assistant.getName();
    }

    @Override
    public Conversation start() {
        var threadRequest = ThreadRequest.builder()
            // .messages(null)
            .build();
        
        var thread = this.openAiService.createThread(threadRequest);
 
        return new AssistantConversation(this.openAiService, this.assistant.getId(), thread);
    }
}
