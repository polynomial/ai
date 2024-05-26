package com.cyster.app.sage;

import com.cyster.ai.weave.impl.advisor.AdvisorServiceImpl;
import com.cyster.ai.weave.impl.scenario.ScenarioServiceImpl;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.AdvisorServiceFactory;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioService;
import com.cyster.ai.weave.service.scenario.ScenarioServiceFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SageConfig {
    
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
        //return loadScenarioService(scenarioLoaders, scenarios)
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
    
    private AdvisorService loadAdvisorService(String openAiApiKey) {
        System.out.println("!!!!!!!!!!!!!!!! sage app config 1");
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/services/" + AdvisorServiceFactory.class.getName());
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println("Found resource: " + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("!!!!!!!!!!!!!!!! sage app config 2");

        ServiceLoader<AdvisorServiceFactory> serviceLoader = ServiceLoader.load(AdvisorServiceFactory.class);
        serviceLoader.forEach(factory -> {
            System.out.println("!!!!! Found factory: " + factory.getClass().getName());
        });
        
        var factory = serviceLoader.findFirst();
        if (factory.isEmpty()) {
            throw new IllegalStateException("No implementation of: " + AdvisorServiceFactory.class.getSimpleName());
        }
        
        return factory.get().createAdvisorService(openAiApiKey);
    }
    
    private ScenarioService loadScenarioService(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        var serviceLoader = ServiceLoader.load(ScenarioServiceFactory.class);
        var factory = serviceLoader.findFirst();
        if (factory.isEmpty()) {
            throw new IllegalStateException("No implementation of: " + ScenarioServiceFactory.class.getSimpleName());
        }
        
        return factory.get().createScenarioService(scenarioLoaders, scenarios);
    }
    
}
