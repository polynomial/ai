package com.cyster.sherpa.service.advisor;

import com.cyster.sherpa.service.conversation.Conversation;

public interface Advisor {
    
    String getName();
    
    ConversationBuilder createConversation();
    
    interface ConversationBuilder {
        
        ConversationBuilder setOverrideInstructions(String instruction);
        
        Conversation start();
    }
}
