package com.cyster.app.store.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RepositoryLoadRequest {
    private String name;
    private String uriPrefix;
    private String loadPath;

    public RepositoryLoadRequest(String name, String uriPrefix, String loadPath) {
        this.name = name;
        this.uriPrefix = uriPrefix;
        this.loadPath = loadPath;
    }

    @JsonProperty(required = true)
    public String getName() {
        return this.name;
    }

    @JsonProperty(required = true)
    public String getUriPrefix() {
        return this.uriPrefix;
    }

    @JsonProperty(required = true)
    public String getLoadPath() {
        return this.loadPath;
    }
}
