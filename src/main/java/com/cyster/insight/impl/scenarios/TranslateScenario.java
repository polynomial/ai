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

import com.cyster.ai.openai.OpenAiFactoryImpl;
import com.cyster.insight.impl.conversation.ChatConversation;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.conversation.ConversationException;
import com.cyster.insight.service.conversation.Message;
import com.cyster.insight.service.scenario.Scenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


@Component
public class TranslateScenario implements Scenario {
    private OpenAiFactoryImpl openAiFactory;
    private Map<String, String> defaultVariables = new HashMap<String, String>() {{
        put("language", "en");
        put("target_language", "fr");
    }};
    
	TranslateScenario(OpenAiFactoryImpl openAiFactory) {
	    this.openAiFactory = openAiFactory;
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
	    return new Builder();
	}
	
	public class Builder implements Scenario.ConversationBuilder {
        Map<String, String> context = Collections.emptyMap();
        Optional<String> access_token = Optional.empty();
            
        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            this.context = context;
            return this;
        }

        @Override
        public ConversationBuilder setAccessToken(String token) {
            this.access_token = Optional.of(token);
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
    	
    		return new LocalizeConversation(new ChatConversation(openAiFactory).addSystemMessage(messageWriter.toString()));
    	}
	}

	private static class LocalizeConversation implements Conversation {
        private ChatConversation chatConversation;
        private Boolean userMessage = false;
        
		LocalizeConversation(ChatConversation chatConversation) {
			this.chatConversation = chatConversation;
		}
		
		@Override
		public void addMessage(String message) {
            this.chatConversation.addMessage(message);			
		}

		@Override
		public Message respond() throws ConversationException {
            if (this.userMessage) {
            	throw new ConversationException("This conversation scenaio requires a user prompt"); 
            }
			return this.chatConversation.respond();
		}

		@Override
		public List<Message> getMessages() {
			return this.chatConversation.getMessages();
		}

	}

}
