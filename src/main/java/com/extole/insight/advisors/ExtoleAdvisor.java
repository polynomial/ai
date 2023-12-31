package com.extole.insight.advisors;


import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;

@Component
public class ExtoleAdvisor implements Advisor {
    public final String NAME = "extole-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    ExtoleDocumentVectorStore extoleDocumentVectorStore;
    
    public ExtoleAdvisor(AdvisorService advisorService, ExtoleDocumentVectorStore extoleDocumentVectorStore) {
        this.advisorService = advisorService;
        this.extoleDocumentVectorStore = extoleDocumentVectorStore;
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
