package com.extole.app.jira.scenarios;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioNotFoundException extends RestException {
    public ScenarioNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ScenarioNotFoundException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
