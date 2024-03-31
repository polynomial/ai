
package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.runbooks.ExtoleTicketRunbookSelectingAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleTicketRunbookScenario.Parameters;

@Component
public class ExtoleTicketRunbookScenario implements Scenario<Parameters, Void> {
    public static String NAME = "extoleTicketRunbook";

    private Advisor<Void> advisor;

    ExtoleTicketRunbookScenario(ExtoleTicketRunbookSelectingAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Find the best Runbook for an Extole ticket";
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
    

}


