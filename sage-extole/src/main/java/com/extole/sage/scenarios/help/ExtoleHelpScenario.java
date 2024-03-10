package com.extole.sage.scenarios.help;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.client.ExtoleClientAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.extole.sage.scenarios.help.ExtoleHelpScenario.Context;

@Component
public class ExtoleHelpScenario implements Scenario<Void, Context> {
    public static String NAME = "extole_help";
    
    private ExtoleClientAdvisor advisor;

    ExtoleHelpScenario(ExtoleClientAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Helps using the Extole Platform";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Context context) {
        var advisorContext = new ExtoleClientAdvisor.Context(context.access_token);
        
        return advisor.createConversation().withContext(advisorContext).start();
    }
    
    public static class Context {
        @JsonProperty(required = true)
        public String access_token;
    }
}
