package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleSupportTicketScenario.Parameters;
import com.extole.sage.session.ExtoleSessionContext;

@Component
public class ExtoleSupportTicketScenario implements Scenario<Parameters, ExtoleSessionContext> {
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
- Wait for the notification to be retrieved, before proceeding.
- Search for client events using the tags from the retrieved notification to see the number of similar client events.
- Add a comment to the ticket providing a very brief summary of the notification and the number of times the related client event has occurred, include the uri to any reports.
- Continue with the sub-classification of notification tickets and follow any steps

Notification tickets can be sub-classified into:

sub-classification: notification-change-in-traffic: mentions "by percentage alerts" in the title
- Add a comment to the ticket summarizing the notification, group by program with a sublist of the fields and the amount by which each field changed
- Note the ticket number, classification and if a comment was added to the ticket

sub-classification - notification-webhook: mentions webhook in the title of the ticket
- Load the Webhook
- add a comment to the ticket summarizing the notification and attempt to identify the problem with the webhook, provide a link to the webhook page https://my.extole.com/tech-center/outbound-webhooks?client_id=$client_id#/$webhook_id
- Note the ticket number, classification and if a comment was added to the ticket

sub-classification - notification-prehandler: mentions prehandler in the title of the ticket
- Load the prehandler
- add a comment to the ticket summarizing the notification and attempt to identify the problem with the prehandler
- Note the ticket number, classification and if a comment was added to the ticket

sub-classification - notification-email-render: mentions email render in the title of the ticket
- from the notification get the campaign_id and zone_name and person_id (sender_person_id)
- add a comment to the ticket mention the zone name and link to the campaign https://my.extole.com/campaign/edit/?client_id=$client_id#/$campaign_id and the person https://my.extole.com/profiles/view?client_id=$client_id#$person_id
- Note the ticket number, classification and if a comment was added to the ticket

sub-classification - notification-other: doesn't match any of the above notification classifications
- Note the ticket number, classification and if a comment was added to the ticket

Classification - prehandler: The ticket ask questions about prehandlers
- load the prehandler
- attempt to understand the problem
- post a comment to the ticket
- Note the ticket number, classification and if a comment was added to the ticket

Classification - wismr: The ticket is a reward or gift card for one or more users 
- do not post a comment to the ticket
- Note the ticket number, classification and if a comment was added to the ticket

Classification - ai test: This ticket exists to test ticketing tools
- add a joke relevant to the ticket as a comment
- Note the ticket number, classification and if a comment was added to the ticket

Classification - other: Ticket could not be classified, in any of the above categories.
- do not post a comment to the ticket
- Note the ticket number, classification and if a comment was added to the ticket


""";

    private Advisor<Void> advisor;

    ExtoleSupportTicketScenario(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Analyzes and comments on Extole Support Tickets";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }
 
    @Override
    public Conversation createConversation(Parameters parameters, ExtoleSessionContext context) {
        return this.advisor.createConversation().setOverrideInstructions(INSTRUCTIONS).start();
    }

    public static class Parameters {
        @JsonProperty(required = true)
        public String ticket_number;
    }
    
    public static class Context {
        @JsonProperty(required = true)
        public String access_token;
    }
}
