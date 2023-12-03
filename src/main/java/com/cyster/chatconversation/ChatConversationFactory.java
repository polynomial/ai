package com.cyster.chatconversation;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.conversation.ConversationFactory;
import com.cyster.openai.OpenAiFactory;

@Component
public class ChatConversationFactory implements ConversationFactory {

	private OpenAiFactory openAiFactory;

	public ChatConversationFactory(OpenAiFactory openAiFactory) {
		this.openAiFactory = openAiFactory;
	}

	@Override
	public String getName() {
		return "chat";
	}

	@Override
	public ChatConversation newConversation(String scenarioName, Map<String, String> context) {
		return new ChatConversation(openAiFactory, scenarioName, context);
	}

}
