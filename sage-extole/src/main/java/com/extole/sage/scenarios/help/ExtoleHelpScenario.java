package com.extole.sage.scenarios.help;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.client.ExtoleClientAdvisor;

@Component
public class ExtoleHelpScenario implements Scenario {
    public static String NAME = "extole_help";
    
    private AdvisorService advisorService;

    private Optional<Advisor<ExtoleClientAdvisor.Context>> advisor = Optional.empty();
    
    private Map<String, String> defaultVariables = new HashMap<String, String>();

    ExtoleHelpScenario(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You are an advisor the support team at Extole a SaaS marketing platform.
""";

            AdvisorBuilder<ExtoleClientAdvisor.Context> builder = this.advisorService.getOrCreateAdvisor(NAME);
            
            builder
                .setInstructions(instructions)
                // .withTool(tool)
                .getOrCreate();
                
            this.advisor = Optional.of(builder.getOrCreate());
        }
        return new ConversationBuilder(this.advisor.get());
    }
    
    public class ConversationBuilder implements Scenario.ConversationBuilder {
        private Advisor<ExtoleClientAdvisor.Context> advisor;
        
        ConversationBuilder(Advisor<ExtoleClientAdvisor.Context> advisor) {
            this.advisor = advisor;
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            return this;
        }

        @Override
        public Conversation start() {
            return this.advisor.createConversation().start();
        }
    }


}
