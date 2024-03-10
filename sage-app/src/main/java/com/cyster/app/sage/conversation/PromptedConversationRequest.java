package com.cyster.app.sage.conversation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PromptedConversationRequest {

    private String scenario = "";
    private Object parameters = null;
    private String prompt = "";

    public String getScenario() {
        return this.scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Object getParameters() {
        return this.parameters;
    }

    public void setParameters(Object parameters) {
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
