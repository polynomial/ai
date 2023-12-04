package com.cyster.scenariostore;

public class ScenarioStoreException extends Exception {
    public ScenarioStoreException(String message) {
        super(message);
    }

    public ScenarioStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}