package com.cyster.sage.app.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.sage.app.RestException;

public class ScenarioNameNotSpecifiedRestException extends RestException {
    public ScenarioNameNotSpecifiedRestException() {
        super(HttpStatus.BAD_REQUEST, "No scenario name specified");
    }

}
