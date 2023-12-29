package com.cyster.insight.impl.advisors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;

@Component
public class MumboJumboAdvisor implements Advisor {
    public final String NAME = "mumbo-jumbo-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    
    public MumboJumboAdvisor(AdvisorService advisorService) {
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
                .setInstructions("You are and advisor of nonsensical terms")
                // .withTool()
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
