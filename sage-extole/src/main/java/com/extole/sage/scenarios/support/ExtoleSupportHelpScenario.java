package com.extole.sage.scenarios.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;

@Component
public class ExtoleSupportHelpScenario implements Scenario {
    public static String NAME = "extole_support_help";
    
    private Advisor advisor;
    
    private Map<String, String> defaultVariables = new HashMap<String, String>();

    ExtoleSupportHelpScenario(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
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
        return new ConversationBuilder(this.advisor);
    }
    
    public class ConversationBuilder implements Scenario.ConversationBuilder {
        private Advisor advisor;
        
        ConversationBuilder(Advisor advisor) {
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
