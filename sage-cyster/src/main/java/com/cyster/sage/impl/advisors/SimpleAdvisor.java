package com.cyster.sage.impl.advisors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;

@Component
public class SimpleAdvisor implements Advisor<Void> {
    public final String NAME = "simple-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    
    public SimpleAdvisor(AdvisorService advisorService) {
      this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions("You are a helpful assistant.");
            
                this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
