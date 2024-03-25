package com.extole.sage.advisors.support.reports;

import org.springframework.core.io.Resource;

public class ExtoleReportConfigurtationException extends Exception {    
    public ExtoleReportConfigurtationException(Resource resource, String message) {
        super(buildMessage(resource, message));
    }

    public ExtoleReportConfigurtationException(Resource resource, String message, Throwable cause) {
        super(buildMessage(resource, message), cause);
    }
    
    private static String buildMessage(Resource resource, String message) {
        return message + " for resource: " + resource.toString();
    }
}
