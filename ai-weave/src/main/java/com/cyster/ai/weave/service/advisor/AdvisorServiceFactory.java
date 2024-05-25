package com.cyster.ai.weave.service.advisor;

public interface AdvisorServiceFactory {
    AdvisorService createAdvisorService(String openAiApiKey);
}
