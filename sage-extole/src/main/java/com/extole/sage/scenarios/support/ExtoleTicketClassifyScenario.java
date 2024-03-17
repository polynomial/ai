
package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleTicketClassifyScenario.Parameters;

@Component
public class ExtoleTicketClassifyScenario implements Scenario<Parameters, Void> {
    public static String NAME = "extole_ticket_classifier";

    private static String INSTRUCTIONS = """
Load the ticket {{ticket_number}}
Take the ticket title and description as the problem description and find the best runbook name for the ticket. 
Respond with:
TICKET_NUMBER - CLASSIFICATION
""";

    private Advisor<Void> advisor;

    ExtoleTicketClassifyScenario(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Classify Extole tickets";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }
 
    @Override
    public Conversation createConversation(Parameters parameters, Void context) {
        return this.advisor.createConversation().setOverrideInstructions(INSTRUCTIONS).start();
    }

    public static class Parameters {
        @JsonProperty(required = true)
        public String ticket_number;
    }
    

}


