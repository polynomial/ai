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

//    private static String INSTRUCTIONS_OLD = """
//    Classification - notification: The ticket starts with the message "This is an automated notification from Extole"
//    - Determine the notification_id (aka event_id) and user_id in https://my.extole.com/notifications/view
//    - Get the notification using the notification_id and user_id
//    - Get the tags from the retrieved notification
//    - Search client events to see if there are more events with the same tags
//    - Add a comment to the ticket: in point form, a very brief summary of the notification and the number of times problem has occurred, include a view_uri to reports.
//    - Post a brief comment to the ticket, summarize the notification
//""";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}} and classify into one of the predefined categories based on the tickets content. 
Classify the ticket, and then perform the associated steps.

Classification - notification: The ticket starts with the message "This is an automated notification from Extole"
- Determine the client_id, notification_id (aka event_id) and user_id from https://my.extole.com/notifications/view
- Get the notification using the notification_id and user_id
- Search client events to see if there are more events with the same tags as the tags on the notification
- Add a comment to the ticket, providing a very brief summary of the notification and the number of times the related client event has occurred, include the uri to any reports.

Notification tickets can be sub-classified into:

sub-classification: notification-change-in-traffic: mentions "by percentage alerts" in the title
- Add a comment to the ticket, summarizing the notification, group by program with a sublist of the fields and the amount by which each field changed
- Note the ticket number and classification

sub-classification - notification-webhook: mentions webhook in the title of the ticket
- Load the webhook
- Add a comment to the ticket, summarize the notification and attempt to identify the problem with the webhook

sub-classification - notification-prehandler: mentions prehandler in the title of the ticket
- Load the prehandler
- Add a comment to the ticket, summarize the notification and attempt to identify the problem with the prehandler

sub-classification - notification-email-render: mentions email render in the title of the ticket
- from the notification get the campaign_id and zone_name and person_id
- Add a comment to the ticket, mention the zone name and link to the campaign https://my.extole.com/campaign/edit/?client_id=$client_id#/$campaign_id and the person https://my.extole.com/profiles/view?client_id=$client_id#$person_id

sub-classification - notification-other: doesn't match any of the above notification classifications
- note the ticket number and classification

Classification - prehandler: The ticket ask questions about prehandlers
- load the prehandler
- attempt to understand the problem
- post a comment to the ticket

Classification - wismr: The ticket is a reward or gift card for one or more users 
- note the ticket number and classification

Classification - ai test: This ticket exists to test ticketing tools
- add a joke relevant to the ticket as a comment

Classification - other: Ticket could not be classified, in any of the above categories.
- note the ticket number and classification

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
