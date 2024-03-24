package com.extole.sage.scenarios.runbooks;

import java.net.URI;

import org.springframework.core.io.Resource;

public class ExtoleRunbookConfigurationException extends Exception {    
    public ExtoleRunbookConfigurationException(Resource resource, String message) {
        super(message + " for resource: " + resource.toString());
    }

    public ExtoleRunbookConfigurationException(URI resource, String message, Throwable cause) {
        super(message, cause);
    }
}
