package com.cyster.sherpa.impl.advisor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ToolError {
    private final String error;
    private final boolean isFatal;
    
    ToolError(String error, boolean isFatal) {
        this.error = error;
        this.isFatal = isFatal;
    }

    @JsonProperty("error")
    public String getError() {
        return this.error;
    }
    
    @JsonProperty("is_fatal")
    public boolean isFatal() {
        return this.isFatal;
    }
    
    String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Tool bad call parameters", e);
        }
    }
}
