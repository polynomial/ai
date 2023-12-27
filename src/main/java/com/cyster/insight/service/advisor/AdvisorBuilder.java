package com.cyster.insight.service.advisor;

import com.cyster.insight.impl.advisor.AdvisorTool;

public interface AdvisorBuilder {

    <T> AdvisorBuilder withTool(AdvisorTool<T> tool);
    
    // ManagedAssistantBuilder withFile();
    
    Advisor getOrCreate();
}
