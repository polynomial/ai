package com.cyster.aiapp;

import java.util.Map;

import org.springframework.ai.core.llm.LlmClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
	
    private final LlmClient llmClient;

    @Autowired
    public RootController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }
    
	@GetMapping("/")
	public String index() {
		return "Hello world\n";
	}

    @GetMapping("/ai-check")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", llmClient.generate(message));
    }

}
