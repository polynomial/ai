package com.cyster.conversationstore;

import com.cyster.conversation.Conversation;

public class ConversationHandle {
	private String id;
	private Conversation conversation;

	ConversationHandle(String id, Conversation conversation) {
		this.id = id;
		this.conversation = conversation;
	}

	public String getId() {
		return this.id;
	}

	public Conversation getConversation() {
		return this.conversation;
	}
}
