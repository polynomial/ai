package com.extole.sage.scenarios.prehandler;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.ExtoleJavascriptPrehandlerActionAdvisor;
import com.extole.sage.advisors.ExtoleJavascriptPrehandlerActionAdvisor.AdminUserToolContext;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.extole.sage.scenarios.prehandler.JavascriptPrehandlerActionScenario.Context;

@Component
public class JavascriptPrehandlerActionScenario implements Scenario<Void, Context> {
    public static String NAME = "extole_prehandler_action";

    private Advisor<AdminUserToolContext> advisor;

    JavascriptPrehandlerActionScenario(ExtoleJavascriptPrehandlerActionAdvisor advisor) {
        this.advisor = advisor;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Helps with writing and debugging prehandlers";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Context context) {
        return new ConversationBuilder(this.advisor).setContext(context).start();
    }

    public class ConversationBuilder {
        private Advisor<AdminUserToolContext> advisor;
        private Context context;

        ConversationBuilder(Advisor<AdminUserToolContext> advisor) {
            this.advisor = advisor;
        }

        public ConversationBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Conversation start() {
            var context = new AdminUserToolContext(this.context.access_token);;

            return this.advisor.createConversation().withContext(context).start();
        }
    }

    public static class Context {
        @JsonProperty(required = true)
        public String access_token;
    }

}
