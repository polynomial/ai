package com.cyster.insight.impl.scenarios.help;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.advisors.SimpleAdvisor;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.cyster.insight.service.scenario.Scenario;


@Component
public class ExtoleHelpScenario implements Scenario {
    private Advisor advisor;

    private Map<String, String> defaultVariables = new HashMap<String, String>() {
    };

    ExtoleHelpScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return "extole-help";
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new Builder(this.advisor);
    }

    
    private static class HelpConversation implements Conversation {
        private Conversation conversation;

        HelpConversation(Conversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public HelpConversation addMessage(String message) {
            this.conversation.addMessage(message);
            return this;
        }

        @Override
        public Message respond() throws ConversationException {
            List<Message> messages = this.conversation.getMessages();
            if (messages.size() == 0 || messages.get(messages.size() - 1).getType() != Message.Type.USER) {
                throw new ConversationException("This conversation scenaio requires a user prompt");
            }
            return this.conversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return this.conversation.getMessages();
        }

    }

    public class Builder implements Scenario.ConversationBuilder {
        private Advisor advisor;
        private Map<String, String> context = Collections.emptyMap();
        
        Builder(Advisor advisor) {
            this.advisor = advisor;
        }
        
        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public Conversation start() {

            /*
            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();
            var instructions = messageWriter.toString();
            */
   
           
            return new HelpConversation(this.advisor.createConversation().start());
        }
    }

 
}
