package com.cyster.conversation;

import java.util.List;

public interface Conversation {

	public void addMessage(String message);

	public Message respond() throws ConversationException;

	public List<Message> getMessages();
	
}
