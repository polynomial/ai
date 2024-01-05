package com.cyster.sage.impl.advisors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sage.service.advisor.Advisor;
import com.cyster.sage.service.advisor.AdvisorService;

// Currently a Scenario creates an Conversation, should create an Assistant, then this would be used
// an Assistant would return a Conversation

@Component
public class CodingAdvisor implements Advisor {
    public final String NAME = "code-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    
    public CodingAdvisor(AdvisorService advisorService) {
      this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions("You are a highly experienced software engineer. You focus on creating simple, highly readable software")
                // .withTool()
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
