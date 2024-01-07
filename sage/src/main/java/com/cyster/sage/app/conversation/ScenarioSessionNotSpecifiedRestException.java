package com.cyster.sage.app.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.sage.app.RestException;

public class ScenarioSessionNotSpecifiedRestException extends RestException {
    public ScenarioSessionNotSpecifiedRestException() {
        super(HttpStatus.BAD_REQUEST, "No scenario session_id specified");
    }
}
