package com.cyster.sage.service.advisor;

import java.nio.file.Path;

import com.cyster.sage.impl.advisor.Tool;

public interface AdvisorBuilder {

    AdvisorBuilder setInstructions(String instruction);
    
    <T> AdvisorBuilder withTool(Tool<T> tool);
    
    AdvisorBuilder withFile(Path path);
    
    Advisor getOrCreate();
}
