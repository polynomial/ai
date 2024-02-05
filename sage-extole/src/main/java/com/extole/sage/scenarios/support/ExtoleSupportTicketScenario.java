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
Load the support ticket {{ticket_number}} and classify into one of the predefined categories based on the tickets content. 
Then take the classification and perform the steps of the associated Runbook. 

- notification: The ticket starts with the message "This is an automated notification from Extole"
- make_good: The ticket is a request to issue a reward to a user
- suspcicious_activity: The ticket talks about suspicious activity
- creative_customization: The ticket requests changes to the UI of the consumer experience.
- other: Ticket could not be classified, in any of the above categories.


Runbook: notifications
- determine the notification_id (aka event_id) and user_id in https://my.extole.com/notifications/view
- get the client_event using the notification_id and user_id, note the associated tags
- search client events to see if there are more events with the same tags
- provide a very brief summary in point form

Runbook: make_good
- identify potential keys to identify the user or event that triggered a reward

Runbook: suspcicious_activity
- identify the user or users that are suspicious
- determine if they have lots of rewards or friends

Runbook: creative_customization
- summarize the content of the ticket

Runbook: creative_customization
- summarize the content of the ticket

Runbook: other
- summarize the content of the ticket


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
