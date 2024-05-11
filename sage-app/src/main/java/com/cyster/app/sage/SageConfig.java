package com.cyster.app.sage;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import com.cyster.assistant.impl.advisor.AdvisorServiceImpl;
import com.cyster.assistant.impl.scenario.ScenarioServiceImpl;
import com.cyster.assistant.service.advisor.AdvisorService;
import com.cyster.assistant.service.scenario.Scenario;
import com.cyster.assistant.service.scenario.ScenarioLoader;
import com.cyster.assistant.service.scenario.ScenarioService;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

@Configuration
public class SageConfig {
    
    @Bean
    public OpenAiService getOpenAiService(OpenAiProperties openAiProperties) {
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            throw new IllegalArgumentException(
                "No Open API key with the property name " + CONFIG_PREFIX + ".api-key");
        }
    
        return createOpenAiService(openAiProperties.getApiKey(),  false);
    }
    
    @Bean
    public AdvisorService getAdvisorService(OpenAiService openAiService) {
        return new AdvisorServiceImpl(openAiService);
    }

    @Bean
    public ScenarioService getScenarioService(OpenAiService openAiService, List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        return new ScenarioServiceImpl(scenarioLoaders, scenarios);
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
    
    private static OpenAiService createOpenAiService(String openApiKey, Boolean debug) {
        if (!debug) {
            return new OpenAiService(openApiKey, Duration.ofSeconds(30));
        }
        
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();  
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client =  OpenAiService.defaultClient(openApiKey, Duration.ofSeconds(30))
            .newBuilder()
            .addInterceptor(logging)
            .build();
        
        Retrofit retrofit = OpenAiService.defaultRetrofit(client, mapper);
        
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        
        return new OpenAiService(api);              
    }
    
}
