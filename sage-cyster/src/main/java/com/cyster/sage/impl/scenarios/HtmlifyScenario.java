package com.cyster.sage.impl.scenarios;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.sage.impl.advisors.SimpleAdvisor;
import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class HtmlifyScenario implements Scenario {
    private Advisor<Void> advisor;

    private Map<String, String> defaultVariables = new HashMap<String, String>();

    HtmlifyScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return "htmlify";
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
            String systemPrompt = """
Convert the input to a HTML marked up fragment.

Everything should be included in <p></p> tags. If there are lists use <ul> or <ol> tags.

Just returned the marked up fragment, nothing else.  
""";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.context);
            messageWriter.flush();
            var instructions = messageWriter.toString();
            
            Conversation conversation  = HtmlifyScenario.this.advisor.createConversation()
                .setOverrideInstructions(instructions)
                .start();
            
            return conversation;
        }
    }

 
}
