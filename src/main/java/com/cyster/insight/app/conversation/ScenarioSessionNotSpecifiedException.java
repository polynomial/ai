package com.cyster.insight.app.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.insight.app.RestException;

public class ScenarioSessionNotSpecifiedException extends RestException {
    public ScenarioSessionNotSpecifiedException() {
        super(HttpStatus.BAD_REQUEST, "No scenario session_id specified");
    }
}
