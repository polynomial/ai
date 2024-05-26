package com.extole.app.jira;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import com.cyster.ai.weave.impl.advisor.AdvisorServiceImpl;
import com.cyster.ai.weave.impl.scenario.ScenarioServiceImpl;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration
public class JiraAppConfig {
    
    @Bean
    public AdvisorService getAdvisorService(@Value("${OPENAI_API_KEY}") String openAiApiKey) {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalArgumentException("OPENAI_API_KEY not defined");
        }

        return new AdvisorServiceImpl.Factory().createAdvisorService(openAiApiKey);
    }
    
    @Bean
    public ScenarioService getScenarioService(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        return new ScenarioServiceImpl.Factory().createScenarioService(scenarioLoaders, scenarios);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
        .json()
        .featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    }
    
    @Bean 
    public Jackson2ObjectMapperBuilder objectMapperBuilder(){
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.failOnUnknownProperties(true);
        return builder;
    }
    
}
