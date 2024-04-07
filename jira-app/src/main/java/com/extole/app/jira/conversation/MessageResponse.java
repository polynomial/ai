package com.extole.app.jira.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageResponse {
	private String type;
	private String content;

	public MessageResponse(String type, String content) {
		this.type = type;
		this.content = content;
	}

	public String getType() {
		return this.type;
	}

	public String getContent() {
		return this.content;
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
