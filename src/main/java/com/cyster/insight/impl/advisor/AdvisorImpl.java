package com.cyster.insight.impl.advisor;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.conversation.Conversation;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;

public class AdvisorImpl implements Advisor {
    private OpenAiService openAiService;
    private Assistant assistant;
    
    public AdvisorImpl(OpenAiService openAiService, Assistant assistant) {
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
            .build();
        
        var thread = this.openAiService.createThread(threadRequest);
 
        return new AdvisorConversation(this.openAiService, this.assistant.getId(), thread);
    }
}
