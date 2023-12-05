package com.cyster.conversationstore;

import com.cyster.conversation.Conversation;
import com.cyster.scenario.Scenario;

public class ConversationHandle {
	private String id;
	private Scenario scenario;
	private Conversation conversation;

	ConversationHandle(String id, Scenario scenario, Conversation conversation) {
		this.id = id;
		this.scenario = scenario;
		this.conversation = conversation;
	}

	public String getId() {
		return this.id;
	}

	public Scenario getScenario() {
		return this.scenario;
	}
	
	public Conversation getConversation() {
		return this.conversation;
	}
}
