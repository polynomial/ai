package com.cyster.openai;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.ai.openai.llm.OpenAiClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.theokanning.openai.service.OpenAiService;

@Component
public class OpenAiFactory {
	
	private final OpenAiService openAiService;
	private final OpenAiClient openAiClient;

	public OpenAiFactory(OpenAiProperties openAiProperties, OpenAiClient openAiClient) {
		if (!StringUtils.hasText(openAiProperties.getApiKey())) {
			throw new IllegalArgumentException(
			    "No Open API key with the property name " + CONFIG_PREFIX + ".api-key");
		}
		
		this.openAiService = new OpenAiService(openAiProperties.getApiKey());
		this.openAiClient = openAiClient;
	}
	
	
	public OpenAiService getService() {
		return this.openAiService;
	}
	
    public OpenAiClient getClient() {
    	return this.openAiClient;
    }
}
