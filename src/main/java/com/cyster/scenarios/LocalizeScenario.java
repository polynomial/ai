package com.cyster.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.chatconversation.ChatConversation;
import com.cyster.conversation.Conversation;
import com.cyster.conversation.ConversationException;
import com.cyster.conversation.Message;
import com.cyster.openai.OpenAiFactory;
import com.cyster.scenario.Scenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import com.cyster.scenario.ScenarioException;


@Component
public class LocalizeScenario implements Scenario {
    private OpenAiFactory openAiFactory;
    private Map<String, String> defaultVariables = new HashMap<String, String>() {{
        put("language", "en");
        put("target_language", "fr");
    }};
    
	LocalizeScenario(OpenAiFactory openAiFactory) {
	    this.openAiFactory = openAiFactory;
	}
	
	@Override
	public String getName() {
		return "localize";
	}

	@Override
	public Set<String> variables() {
		return defaultVariables.keySet();
	}

	@Override
	public Conversation startConversation(Map<String, String> context) {
		String systemPrompt = "Please translate messages from {{language}} to {{target_language}}.";

		MustacheFactory mostacheFactory = new DefaultMustacheFactory();
		Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
		var messageWriter = new StringWriter();
		mustache.execute(messageWriter, context);
		messageWriter.flush();	
	
		return new LocalizeConversation(new ChatConversation(openAiFactory, this.getName()).addSystemMessage(messageWriter.toString()));
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

		@Override
		public String getScenarioName() {
			return this.chatConversation.getScenarioName();
		}
		
	}

}
