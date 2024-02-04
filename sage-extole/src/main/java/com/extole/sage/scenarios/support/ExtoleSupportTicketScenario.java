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
public class ExtoleSupportTicketScenario implements Scenario {
    public static String NAME = "extole_support_ticket";
    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}} and classify into one of the predefined categories based on the tickets content 

- notification: The ticket starts with the message "This is an automated notification from Extole"
- make_good: The ticket is a request to issue a reward to a user
- suspcicious_activity: The ticket talks about suspicious activity
- creative_customization_minor: The ticket requests small changes to the UI of the consumer experience.
- other: Ticket could not be classified, in any of the above categories.
""";
    
    private Advisor<Void> advisor;
    
    private Map<String, String> defaultVariables = new HashMap<String, String>() {
        {
            put("ticket_number", "");
        }
    };

    ExtoleSupportTicketScenario(ExtoleSupportAdvisor advisor) {
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
            
            return this.advisor.createConversation().setOverrideInstructions(INSTRUCTIONS).start();
        }
    }
}
