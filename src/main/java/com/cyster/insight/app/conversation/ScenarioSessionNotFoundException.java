package com.cyster.insight.app.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.insight.app.RestException;

public class ScenarioSessionNotFoundException extends RestException {
    private String sessionId;

    public ScenarioSessionNotFoundException(String sessionId) {
        super(HttpStatus.BAD_REQUEST, getMessage(sessionId));
        this.sessionId = sessionId;
    }

    ScenarioSessionNotFoundException(String sessionId, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, getMessage(sessionId), cause);
        this.sessionId = sessionId;
    }

    public String getSessionid() {
        return this.sessionId;
    }

    private static String getMessage(String session_id) {
        return "Scenario session_id '" + session_id + "' not found";
    }
}
