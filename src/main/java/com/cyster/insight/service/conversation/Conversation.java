package com.cyster.insight.service.conversation;

import java.util.List;

public interface Conversation {

    public Conversation addMessage(String message);

    public Message respond() throws ConversationException;

    public List<Message> getMessages();

}
