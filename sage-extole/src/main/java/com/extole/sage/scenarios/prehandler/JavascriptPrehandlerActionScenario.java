package com.extole.sage.scenarios.prehandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.ExtoleJavascriptPrehandlerActionAdvisor;
import com.extole.sage.advisors.ExtoleJavascriptPrehandlerActionAdvisor.AdminUserToolContext;

@Component
public class JavascriptPrehandlerActionScenario implements Scenario {
    public static String NAME = "extole_prehandler_action";

    private Advisor<AdminUserToolContext> advisor;

    private Map<String, String> defaultVariables = new HashMap<String, String>() {
        {
            put("extole_user_token", "");
        }
    };

    JavascriptPrehandlerActionScenario(ExtoleJavascriptPrehandlerActionAdvisor advisor) {
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
        private Advisor<AdminUserToolContext> advisor;
        private String userToken = null;

        ConversationBuilder(Advisor<AdminUserToolContext> advisor) {
            this.advisor = advisor;
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.userToken = context.get("extole_user_token");
            return this;
        }

        @Override
        public Conversation start() {
            var context = new AdminUserToolContext(this.userToken);

            return this.advisor.createConversation().withContext(context).start();
        }
    }

}
