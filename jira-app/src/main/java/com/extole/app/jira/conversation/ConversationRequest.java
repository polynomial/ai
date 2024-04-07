package com.extole.app.jira.conversation;

import java.util.HashMap;
import java.util.Map;

public class ConversationRequest {
    private String scenarioName;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public ConversationRequest(String scenarioName, Map<String, Object> parameters) {
        this.scenarioName = scenarioName;
        this.parameters = parameters;
    }

    public String getScenarioName() {
        return this.scenarioName;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }
}
