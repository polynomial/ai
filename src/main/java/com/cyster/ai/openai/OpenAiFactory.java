package com.cyster.ai.openai;

import com.theokanning.openai.service.OpenAiService;

public interface OpenAiFactory {
	
	OpenAiService getService();
	
}
