package com.cyster.ai.weave.service.advisor;

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
    
    // TODO remove use advisor
    TooledChatConversation createTooledChatConversation();


    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);
    
    <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder();

}
