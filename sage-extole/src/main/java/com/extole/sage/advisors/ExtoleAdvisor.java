package com.extole.sage.advisors;


import java.util.Optional;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;

public class ExtoleAdvisor implements Advisor<Void> {
    public final String NAME = "extoleAdvisor";

    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    
    public ExtoleAdvisor(AdvisorService advisorService) {
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
            builder.setInstructions("You are a helpful assistant.");
            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }
    
    

}
