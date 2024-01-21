package com.cyster.sherpa.service.advisor;

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
}
