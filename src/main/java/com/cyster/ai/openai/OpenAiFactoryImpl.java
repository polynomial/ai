package com.cyster.ai.openai;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import java.time.Duration;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import okhttp3.logging.HttpLoggingInterceptor;


@Component
public class OpenAiFactoryImpl implements OpenAiFactory {
	
	private final OpenAiService openAiService;

	public OpenAiFactoryImpl(OpenAiProperties openAiProperties) {
		if (!StringUtils.hasText(openAiProperties.getApiKey())) {
			throw new IllegalArgumentException(
			    "No Open API key with the property name " + CONFIG_PREFIX + ".api-key");
		}
		
		this.openAiService = createOpenAiService(openAiProperties.getApiKey(),  true);
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
	
	public OpenAiService getService() {
		return this.openAiService;
	}

}
