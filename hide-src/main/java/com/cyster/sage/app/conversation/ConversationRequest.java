package com.cyster.sage.app.conversation;

import java.util.HashMap;
import java.util.Map;

public class ConversationRequest {
    private String scenarioName;
    private Map<String, String> context = new HashMap<String, String>();

    public ConversationRequest(String scenarioName, Map<String, String> context) {
        this.scenarioName = scenarioName;
        this.context = context;
    }

    public String getScenarioName() {
        return this.scenarioName;
    }

    public Map<String, String> getContext() {
        return this.context;
    }
}
