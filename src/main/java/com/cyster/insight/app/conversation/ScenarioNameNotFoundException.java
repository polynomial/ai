package com.cyster.insight.app.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.insight.app.RestException;

public class ScenarioNameNotFoundException extends RestException {
    private String scenarioName;

    public ScenarioNameNotFoundException(String name) {
        super(HttpStatus.BAD_REQUEST, getMessage(name));
        this.scenarioName = name;
    }

    ScenarioNameNotFoundException(String name, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, getMessage(name), cause);
    }

    public String getScenarioName() {
        return this.scenarioName;
    }

    private static String getMessage(String name) {
        return "Scenario Name '" + name + "' not found";
    }
}
