package com.cyster.insight.app.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.insight.app.RestException;

public class ScenarioNameNotSpecifiedException extends RestException {
    public ScenarioNameNotSpecifiedException() {
        super(HttpStatus.BAD_REQUEST, "No scenario name specified");
    }

}
