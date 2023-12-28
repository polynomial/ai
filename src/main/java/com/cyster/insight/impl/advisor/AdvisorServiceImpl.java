package com.cyster.insight.impl.advisor;


import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.AdvisorBuilder;
import com.cyster.insight.service.advisor.AdvisorService;
import com.cyster.insight.service.openai.OpenAiFactory;
import com.theokanning.openai.service.OpenAiService;


// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

@Component
public class AdvisorServiceImpl implements AdvisorService {

    private OpenAiService openAiService;
    
    public AdvisorServiceImpl(OpenAiFactory openAifactory) {
        this.openAiService = openAifactory.getService();
    }
    
    public AdvisorBuilder getOrCreateAdvisor(String name) {
        // TODO support returning other advisor implementations: ChatAdvisor, TooledChatAdvisor
        return new AssistantAdvisorImpl.Builder(this.openAiService, name);    
    }
     
    
 
}
