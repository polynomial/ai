package com.cyster.ai.weave.service.conversation;

import java.util.List;

public interface Conversation {

    public Conversation addMessage(String message);

    public Message respond() throws ConversationException;

    public List<Message> getMessages();

}
