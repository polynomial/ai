package com.cyster.insight.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.insight.impl.advisors.SimpleAdvisor;
import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.scenario.Scenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class MumboJumboScenario implements Scenario {
    private Advisor advisor;

    private Map<String, String> defaultVariables = new HashMap<String, String>() {
    };

    MumboJumboScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return "mumbo_jumbo";
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new Builder();
    }

    
    public class Builder implements Scenario.ConversationBuilder {
        private Map<String, String> context = Collections.emptyMap();
        
        Builder() {
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
            
            Conversation conversation  = MumboJumboScenario.this.advisor.createConversation()
                .setOverrideInstructions(instructions)
                .start();
            
            return conversation;
        }
    }

 
}
