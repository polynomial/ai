package com.cyster.insight.impl.assistants;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.assistant.ManagedAssistantService;
import com.cyster.insight.service.assistant.ManagedAssistant;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.openai.OpenAiFactory;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.service.OpenAiService;
   
@Component
public class CodingAssistant implements ManagedAssistant {

    private final String CODING_ASSISTANT = "code";
        
    private OpenAiService openAiService;
    private ManagedAssistantService assitantService;
    private Assistant assistant;
    
    public CodingAssistant(OpenAiFactory openAiFactory, ManagedAssistantService assistantService) {
        this.openAiService = openAiFactory.getService();
        this.assitantService = assistantService;
 
        this.assistant = null;
    }
    
    @Override
    public String getName() {
        return CODING_ASSISTANT;
    }

    @Override
    public Conversation start() {
        // TODO Auto-generated method stub
        return null;
    }

}
