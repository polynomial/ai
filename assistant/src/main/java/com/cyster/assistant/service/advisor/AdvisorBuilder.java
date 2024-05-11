package com.cyster.assistant.service.advisor;

import java.nio.file.Path;

import com.cyster.assistant.impl.advisor.Tool;

public interface AdvisorBuilder<C> {

    AdvisorBuilder<C> setInstructions(String instruction);
    
    <T> AdvisorBuilder<C> withTool(Tool<T, C> tool);
    
    AdvisorBuilder<C> withFile(Path path);
    
    Advisor<C> getOrCreate();
}
