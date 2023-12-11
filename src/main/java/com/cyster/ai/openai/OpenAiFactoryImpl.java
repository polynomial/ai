package com.cyster.ai.openai;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import java.time.Duration;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.ai.openai.llm.OpenAiClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cyster.insight.service.openai.OpenAiFactory;
import com.theokanning.openai.service.OpenAiService;

@Component
public class OpenAiFactoryImpl implements OpenAiFactory {
	
	private final OpenAiService openAiService;
	private final OpenAiClient openAiClient;

	public OpenAiFactoryImpl(OpenAiProperties openAiProperties, OpenAiClient openAiClient) {
		if (!StringUtils.hasText(openAiProperties.getApiKey())) {
			throw new IllegalArgumentException(
			    "No Open API key with the property name " + CONFIG_PREFIX + ".api-key");
		}
		
		this.openAiService = new OpenAiService(openAiProperties.getApiKey(),  Duration.ofSeconds(30));
		this.openAiClient = openAiClient;
	}
	
	
	public OpenAiService getService() {
		return this.openAiService;
	}
	
    public OpenAiClient getClient() {
    	return this.openAiClient;
    }
}
