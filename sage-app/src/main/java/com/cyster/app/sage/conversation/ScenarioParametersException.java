package com.cyster.app.sage.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioParametersException  extends RestException {
    private String scenarioName;

    public ScenarioParametersException(String name) {
        super(HttpStatus.BAD_REQUEST, getMessage(name));
        this.scenarioName = name;
    }

    ScenarioParametersException(String name, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, getMessage(name), cause);
    }

    public String getScenarioName() {
        return this.scenarioName;
    }

    private static String getMessage(String name) {
        return "Scenario Name '" + name + "' not found";
    }
}
