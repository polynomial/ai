package com.cyster.sage.impl.scenarios;


import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

import com.cyster.sage.impl.advisors.SimpleAdvisor;
import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.cyster.sage.impl.scenarios.HtmlifyScenario.Context;

@Component
public class HtmlifyScenario implements Scenario<Void, Context> {
    private static final String NAME = "htmlify";
        
    private Advisor<Void> advisor;

    HtmlifyScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Marks up text with html";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Context context) {
        return new Builder(this.advisor).setContext(context).start();
    }

    public static class Builder {
        private Advisor<Void> advisor;
        private Context context;
        
        Builder(Advisor<Void> advisor) {
            this.advisor = advisor;
        }
        
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

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
            
            Conversation conversation  = this.advisor.createConversation()
                .setOverrideInstructions(instructions)
                .start();
            
            return conversation;
        }
    }
 
    public static class Context {
        @JsonProperty(required = true)
        public String access_token;
    }

}
