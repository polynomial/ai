package com.cyster.conversation;

import java.util.Map;

public interface ConversationFactory {

	public String getName();

	public Conversation newConversation(String scenarioName, Map<String, String> context);
}
