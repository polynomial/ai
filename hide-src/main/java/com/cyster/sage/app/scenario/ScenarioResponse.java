package com.cyster.sage.app.scenario;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScenarioResponse {

    private String name;
    private Set<String> variables;

    public ScenarioResponse(String name, Set<String> variables) {
        this.name = name;
        this.variables = variables;
    }

    public String getName() {
        return this.name;
    }

    public Set<String> getVariables() {
        return this.variables;
    }

    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        private String name;
        private Set<String> variables;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVariables(Set<String> variables) {
            this.variables = variables;
            return this;
        }

        public ScenarioResponse build() {
            return new ScenarioResponse(this.name, this.variables);
        }
    }
}
