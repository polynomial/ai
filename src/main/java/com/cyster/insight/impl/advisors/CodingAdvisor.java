package com.cyster.insight.impl.advisors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;
import com.cyster.insight.service.conversation.Conversation;

// Currently a Scenario creates an Conversation, should create an Assistant, then this would be used
// an Assistant would return a Conversation

@Component
public class CodingAdvisor implements Advisor {
    public final String CODING_ADVISOR = "code-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    
    public CodingAdvisor(AdvisorService advisorService) {
      this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return CODING_ADVISOR;
    }

    @Override
    public Conversation start() {
        if (this.advisor.isEmpty()) {
            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(CODING_ADVISOR)
                .setInstructions("You are a higly experienced software engineer. You focus on creating simple, highly readable software")
                // .withTool()
                .getOrCreate());
        }
        return this.advisor.get().start();
    }

}
