package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.runbooks.ExtoleTicketRunbookExecutingAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleSupportTicketScenario.Parameters;

@Component
public class ExtoleSupportTicketScenario implements Scenario<Parameters, Void> {
    public static String NAME = "extole_support_ticket";

    private Advisor<Void> advisor;

    ExtoleSupportTicketScenario(ExtoleTicketRunbookExecutingAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Analyzes and comment on Extole Support Tickets";
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
        return this.advisor.createConversation().start();
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
