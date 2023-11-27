package com.cyster.conversation;

import java.util.Map;

public interface ConversationFactory {

	public String getName();

	public Conversation newConversation(Map<String, String> context);
}
