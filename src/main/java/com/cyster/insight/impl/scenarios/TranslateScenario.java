package com.cyster.insight.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.advisor.AdvisorService;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.cyster.insight.service.scenario.Scenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class TranslateScenario implements Scenario {
    private AdvisorService advisorService;

    private Map<String, String> defaultVariables = new HashMap<String, String>() {
        {
            put("language", "en");
            put("target_language", "fr");
        }
    };

    TranslateScenario(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @Override
    public String getName() {
        return "translate";
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new Builder(this.advisorService);
    }

    
    private static class LocalizeConversation implements Conversation {
        private Conversation conversation;

        LocalizeConversation(Conversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public LocalizeConversation addMessage(String message) {
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
        private AdvisorService advisorService;
        private Optional<Advisor> advisor = Optional.empty();
        private Map<String, String> context = Collections.emptyMap();
        
        Builder(AdvisorService advisorService) {
            this.advisorService = advisorService;
        }
        
        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public Conversation start() {
            String systemPrompt = "Please translate messages from {{language}} to {{target_language}}.";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();
            var instructions = messageWriter.toString();
            
            if (this.advisor.isEmpty()) {
                this.advisor = Optional.of(this.advisorService.getOrCreateAdvisor("translator")
                    .setInstructions(instructions)
                    .getOrCreate());
            }
           
            return new LocalizeConversation(this.advisor.get().start());
        }
    }

 
}
