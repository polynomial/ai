package com.cyster.ai.service.scenario;

public class ScenarioException extends Exception {
    public ScenarioException(String message) {
        super(message);
    }

    public ScenarioException(String message, Throwable cause) {
        super(message, cause);
    }
}