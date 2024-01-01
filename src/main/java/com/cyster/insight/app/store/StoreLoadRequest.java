package com.cyster.insight.app.store;

public class StoreLoadRequest {
    private String name;
    private String uriPrefix;
    private String loadPath;
    
    public StoreLoadRequest(String name, String uriPrefix, String loadPath) {
        this.name = name;
        this.uriPrefix = uriPrefix;
        this.loadPath = loadPath;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getUriPrefix() {
        return this.uriPrefix;
    }
    
    public String getPath() {
        return this.loadPath;
    }
}

