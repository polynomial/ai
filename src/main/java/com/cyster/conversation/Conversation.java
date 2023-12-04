package com.cyster.conversation;

import java.util.List;
import java.util.Map;

public interface Conversation {

	public void addMessage(String message);

	public Message respond() throws ConversationException;

	public List<Message> getMessages();

	public String getScenarioName();
}
