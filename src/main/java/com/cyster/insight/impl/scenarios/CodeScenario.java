package com.cyster.insight.impl.scenarios;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.scenario.Scenario;


@Component
public class CodeScenario implements Scenario {
    private static final String NAME = "code";
    
    private AdvisorService advisorService;
    private Map<String, String> defaultVariables = new HashMap<String, String>();
    private Optional<Advisor> advisor = Optional.empty();
    
    CodeScenario(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> variables() {
        return this.defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        if (this.advisor.isEmpty()) {
            this.advisor = Optional.of(advisorService.getOrCreateAdvisor(NAME)
                // .withTool()
                .getOrCreate());
        }
        
        return new Builder(this.advisor.get());
    }

    
    public class Builder implements Scenario.ConversationBuilder {
        private Advisor managedAssistant;

        Builder(Advisor managedAssistant) {
            this.managedAssistant = managedAssistant;
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            return this;
        }

        @Override
        public ConversationBuilder setAccessToken(String token) {
            return this;
        }

        @Override
        public Conversation start() {
            return this.managedAssistant.start();
        }
    }

}
