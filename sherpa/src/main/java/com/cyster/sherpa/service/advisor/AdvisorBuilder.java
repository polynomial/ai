package com.cyster.sherpa.service.advisor;

import java.nio.file.Path;

import com.cyster.sherpa.impl.advisor.Tool;

public interface AdvisorBuilder<C> {

    AdvisorBuilder<C> setInstructions(String instruction);
    
    <T> AdvisorBuilder<C> withTool(Tool<T, C> tool);
    
    AdvisorBuilder<C> withFile(Path path);
    
    Advisor<C> getOrCreate();
}
