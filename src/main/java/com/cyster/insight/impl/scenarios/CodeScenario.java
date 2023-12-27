package com.cyster.insight.impl.scenarios;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.assistant.ManagedAssistantService;

import com.cyster.insight.service.assistant.ManagedAssistant;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.scenario.Scenario;


@Component
public class CodeScenario implements Scenario {
    private static final String NAME = "code";
    
    private ManagedAssistant managedAssistant;
    private Map<String, String> defaultVariables = new HashMap<String, String>();
    
    CodeScenario(ManagedAssistantService managedAssistantService) {
        this.managedAssistant =  managedAssistantService.createAssistant(NAME)
            //.withTool(null)
            .create();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> variables() {
        return this.defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new Builder(this.managedAssistant);
    }

    
    public class Builder implements Scenario.ConversationBuilder {
        private Map<String, String> context = Collections.emptyMap();
        private ManagedAssistant managedAssistant;

        Builder(ManagedAssistant managedAssistant) {
            this.managedAssistant = managedAssistant;
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public ConversationBuilder setAccessToken(String token) {
            return this;
        }

        @Override
        public Conversation start() {
            return this.managedAssistant.start();
        }
    }

}
