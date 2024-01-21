package com.cyster.sherpa.service.advisor;

import com.cyster.sherpa.service.conversation.Conversation;

public interface Advisor<C> {
    
    String getName();
    
    ConversationBuilder<C> createConversation();
    
    interface ConversationBuilder<C> {
        
        ConversationBuilder<C> withContext(C context);
        
        ConversationBuilder<C> setOverrideInstructions(String instruction);
        
        // ConversationBuilder<C> addMessage(Type type, String message);
        
        Conversation start();
    }
}
