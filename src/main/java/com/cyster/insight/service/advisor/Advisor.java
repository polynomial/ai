package com.cyster.insight.service.advisor;

import com.cyster.insight.service.conversation.Conversation;

public interface Advisor {
    
    String getName();
    
    Conversation start();
}
