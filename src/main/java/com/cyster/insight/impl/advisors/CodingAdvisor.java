package com.cyster.insight.impl.advisors;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.advisor.AdvisorServiceImpl;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.openai.OpenAiFactory;

// Currently a Scenario creates an Conversation, should create an Assistant, then this would be used
// an Assistant would return a Conversation

@Component
public class CodingAdvisor implements Advisor {
    private final String CODING_ADVISOR = "code-advisor";
      
    public CodingAdvisor(OpenAiFactory openAiFactory, AdvisorServiceImpl assistantService) {
    }
    
    @Override
    public String getName() {
        return CODING_ADVISOR;
    }

    @Override
    public Conversation start() {
        throw new RuntimeException("not implemented");
    }

}
