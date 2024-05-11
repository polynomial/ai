package com.cyster.assistant.service.advisor;

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
}
