package com.cyster.insight.service.assistant;

import com.cyster.insight.service.conversation.Conversation;

public interface ManagedAssistant {
    
    String getName();
    
    Conversation start();
}
