package com.extole.sage.scenarios.help;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.client.ExtoleClientAdvisor;

@Component
public class ExtoleHelpScenario implements Scenario {
    public static String NAME = "extole_help";
    
    private Advisor advisor;
    
    private Map<String, String> defaultVariables = new HashMap<String, String>();

    ExtoleHelpScenario(ExtoleClientAdvisor advisor) {
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
