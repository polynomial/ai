package com.extole.insight.advisors;


import java.util.Optional;

import com.cyster.ai.vector.simple.SimpleVectorStoreService;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;

public class ExtoleAdvisor implements Advisor {
    public final String NAME = "extole-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    SimpleVectorStoreService simpleVectorStoreService;
    
    public ExtoleAdvisor(AdvisorService advisorService, SimpleVectorStoreService simpleVectorStoreService) {
        this.advisorService = advisorService;
        this.simpleVectorStoreService = simpleVectorStoreService;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions("You are a helpful assistant.")
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }
    
    

}
