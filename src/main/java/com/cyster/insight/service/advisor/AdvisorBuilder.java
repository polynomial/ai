package com.cyster.insight.service.advisor;

import java.nio.file.Path;

import com.cyster.insight.impl.advisor.Tool;

public interface AdvisorBuilder {

    AdvisorBuilder setInstructions(String instruction);
    
    <T> AdvisorBuilder withTool(Tool<T> tool);
    
    AdvisorBuilder withFile(Path path);
    
    Advisor getOrCreate();
}
