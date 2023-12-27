package com.cyster.insight.service.assistant;

import com.cyster.insight.impl.conversation.ChatTool;

public interface ManagedAssistantBuilder {

    <T> ManagedAssistantBuilder withTool(ChatTool<T> tool);
    
    // ManagedAssistantBuilder withFile();
    
    ManagedAssistant getOrCreate();
}
