package com.cyster.ai.weave.impl.advisor;

public class RetryableAdvisorConversationException extends AdvisorConversationException {
    public RetryableAdvisorConversationException(String message) {
        super(message);
    }

    public RetryableAdvisorConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}
