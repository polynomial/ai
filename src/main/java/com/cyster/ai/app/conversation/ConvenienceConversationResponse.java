package com.cyster.ai.app.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvenienceConversationResponse extends ConversationResponse {
	private String response;

	public ConvenienceConversationResponse(ConversationResponse conversation, String response) {
		super(conversation.getId(), conversation.getScenario(), conversation.getMessages());
		this.response = response;
	}

	public String getResponse() {
		return this.response;
	}

	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
