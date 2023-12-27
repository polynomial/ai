package com.cyster.insight.service.advisor;

import com.cyster.insight.impl.conversation.ChatTool;

public interface AdvisorBuilder {

    <T> AdvisorBuilder withTool(ChatTool<T> tool);
    
    // ManagedAssistantBuilder withFile();
    
    Advisor getOrCreate();
}
