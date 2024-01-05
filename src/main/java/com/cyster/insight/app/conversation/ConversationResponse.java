package com.cyster.insight.app.conversation;

import java.util.ArrayList;
import java.util.List;

import com.cyster.sage.service.conversation.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConversationResponse {
	private String id;
	private String scenario;
	private List<MessageResponse> messages;

	protected ConversationResponse(String id, String scenario, List<MessageResponse> messages) {
		this.id = id;
		this.scenario = scenario;
		this.messages = messages;
	}

	public String getId() {
		return this.id;
	}

	public String getScenario() {
		return scenario;
	}

	public List<MessageResponse> getMessages() {
		return messages;
	}

	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static class Builder {
		private String id;
		private String scenario;
		private List<MessageResponse> messages;

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setScenario(String scenario) {
			this.scenario = scenario;
			return this;
		}

		public Builder setMessages(List<Message> messages) {
			var response = new ArrayList<MessageResponse>();
			for (var message : messages) {
				response.add(new MessageResponse(message.getType().toString(), message.getContent()));
			}
			this.messages = response;
			return this;
		}

		public ConversationResponse build() {
			return new ConversationResponse(this.id, this.scenario, this.messages);
		}
	}
}
