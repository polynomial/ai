package com.cyster.insight.impl.scenarios;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.advisors.CodingAdvisor;
import com.cyster.sage.service.advisor.Advisor;
import com.cyster.sage.service.conversation.Conversation;
import com.cyster.sage.service.conversation.ConversationException;
import com.cyster.sage.service.conversation.Message;
import com.cyster.sage.service.scenario.Scenario;

@Component
public class CodeScenario implements Scenario {
    private static final String NAME = "code";
    
    private Advisor advisor;
    private Map<String, String> defaultVariables = new HashMap<String, String>();
    
    CodeScenario(CodingAdvisor codingAdvisor) {
        this.advisor = codingAdvisor;
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
        return new Builder(this.advisor);
    }

    private static class CodeConversation implements Conversation {
        private Conversation advisorConversation;
        
        CodeConversation(Conversation conversation, Map<String, String> context) {
            this.advisorConversation = conversation;
        }
        
        @Override
        public Conversation addMessage(String message) {
           
            advisorConversation.addMessage(message);
            return this;
        }

        @Override
        public Message respond() throws ConversationException {
            return advisorConversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return advisorConversation.getMessages();
        }
        
    }
    
    public static class Builder implements Scenario.ConversationBuilder {
        private Advisor advisor;
        private Map<String, String> context;
        
        Builder(Advisor advisor) {
            this.advisor = advisor;
            this.context = Collections.emptyMap();
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public Conversation start() {
            return new CodeConversation(this.advisor.createConversation().start(), this.context);
        }
    }
    

}
