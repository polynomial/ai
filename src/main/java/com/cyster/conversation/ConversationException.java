package com.cyster.conversation;

public class ConversationException extends Exception {
    public ConversationException(String message) {
        super(message);
    }

    public ConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}
