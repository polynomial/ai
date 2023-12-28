package com.cyster.insight.service.advisor;

import com.cyster.insight.impl.advisor.AdvisorTool;

public interface AdvisorBuilder {

    AdvisorBuilder setInstructions(String instruction);
    
    <T> AdvisorBuilder withTool(AdvisorTool<T> tool);
    
    // ManagedAssistantBuilder withFile();
    
    Advisor getOrCreate();
}
