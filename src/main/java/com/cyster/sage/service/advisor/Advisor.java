package com.cyster.sage.service.advisor;

import com.cyster.sage.service.conversation.Conversation;

public interface Advisor {
    
    String getName();
    
    ConversationBuilder createConversation();
    
    interface ConversationBuilder {
        
        ConversationBuilder setOverrideInstructions(String instruction);
        
        Conversation start();
    }
}
