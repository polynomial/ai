package com.cyster.sage.service.conversation;

import java.util.List;

public interface Conversation {

    public Conversation addMessage(String message);

    public Message respond() throws ConversationException;

    public List<Message> getMessages();

}
