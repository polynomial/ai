package com.extole.sage.advisors.client;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorService;
import com.cyster.sherpa.service.conversation.Conversation;

@Component
public class ExtoleClientAdvisor implements Advisor {
    public final String NAME = "extole-client";

    private AdvisorService advisorService;
    private Optional<Advisor> advisor = Optional.empty();
    
    public ExtoleClientAdvisor(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You help with questions around using the Extole SaaS Marketing platform.
""";

            this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor(NAME)
                .setInstructions(instructions)
                // TOOOLS
                .getOrCreate());
        }
        
        return this.advisor.get().createConversation();
    }

    public static class ExtoleClientConversationBuilder implements ConversationBuilder {
        private Advisor advisor;
        private Optional<String> userAccessToken = Optional.empty();
        private Optional<String> instructions = Optional.empty();
        
        ExtoleClientConversationBuilder(Advisor advisor) {
            this.advisor = advisor;
        }
        
        public ExtoleClientConversationBuilder setUserAccessToken(String token) {
            this.userAccessToken = Optional.of(token);
            return this;
        }
        
        @Override
        public ConversationBuilder setOverrideInstructions(String instructions) {
            this.instructions = Optional.of(instructions);
            return null;
        }

        @Override
        public Conversation start() {
            return this.advisor.createConversation().start();
        }
        
    }
}
