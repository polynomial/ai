package com.extole.sage.advisors;


import java.util.Optional;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorService;

public class ExtoleAdvisor implements Advisor {
    public final String NAME = "extole-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    
    public ExtoleAdvisor(AdvisorService advisorService) {
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
                .setInstructions("You are a helpful assistant.")
                .getOrCreate());
        }
        return this.advisor.get().createConversation();
    }
    
    

}
