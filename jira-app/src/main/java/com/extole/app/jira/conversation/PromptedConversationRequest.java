package com.extole.app.jira.conversation;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PromptedConversationRequest {

    private String scenario = "";
    private Map<String, Object> parameters = null;
    private String prompt = "";

    public String getScenario() {
        return this.scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
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
