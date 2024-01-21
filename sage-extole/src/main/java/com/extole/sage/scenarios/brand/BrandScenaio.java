package com.extole.sage.scenarios.brand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.brand.ExtoleBrandAdvisor;

@Component
public class BrandScenaio implements Scenario {
    public static String NAME = "extole_brand";

    private Advisor<Void> advisor;

    private Map<String, String> defaultVariables = new HashMap<String, String>();

    BrandScenaio(ExtoleBrandAdvisor advisor) {
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
        private Advisor<Void> advisor;

        ConversationBuilder(Advisor<Void> advisor) {
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
