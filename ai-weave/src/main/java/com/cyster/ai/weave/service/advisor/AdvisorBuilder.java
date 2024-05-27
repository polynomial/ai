package com.cyster.ai.weave.service.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.scenario.Id;
import com.cyster.ai.weave.service.scenario.VectorStore;

public interface AdvisorBuilder<C> {

    AdvisorBuilder<C> setInstructions(String instruction);

    AdvisorBuilder<C> withVectorStore(Id<VectorStore> id);  // TODO make a specialized tool

    <T> AdvisorBuilder<C> withTool(Tool<T, C> tool);
    
    AdvisorBuilder<C> withFile(Path path);
    
    Advisor<C> getOrCreate();
}
