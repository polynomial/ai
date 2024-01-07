package com.cyster.app.sage.conversation;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PromptedConversationRequest {

    private String scenario = "";
    private Map<String, String> context = Collections.emptyMap();
    private String prompt = "";

    public String getScenario() {
        return this.scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Map<String, String> getContext() {
        return this.context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
