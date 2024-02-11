package com.extole.app.jira;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.cyster.sherpa.impl.advisor.AdvisorServiceImpl;
import com.cyster.sherpa.impl.scenario.ScenarioServiceImpl;
import com.cyster.sherpa.service.advisor.AdvisorService;
import com.cyster.sherpa.service.scenario.Scenario;
import com.cyster.sherpa.service.scenario.ScenarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

@Configuration
public class JiraAppConfig {
    
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
        return new AdvisorServiceImpl(openAiService);  // TODO use modules
    }
 
    @Bean
    public ScenarioService getScenarioService(OpenAiService openAiService, List<Scenario> scenarios) {
        return new ScenarioServiceImpl(scenarios);  // TODO use modules
    }
    
    private static OpenAiService createOpenAiService(String openApiKey, Boolean debug) {
        if (!debug) {
            return new OpenAiService(openApiKey,  Duration.ofSeconds(30));
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
