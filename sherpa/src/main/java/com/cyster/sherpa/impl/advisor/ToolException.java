package com.cyster.sherpa.impl.advisor;

public class ToolException extends Exception {
    private String localMessage = "";
    
    public ToolException(String message) {
        super(message);
    }

    public ToolException(String message, String localMessage) {
        super(message);
        this.localMessage = localMessage;
    }
    
    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ToolException(String message, String localMessage, Throwable cause) {
        super(message, cause);
        this.localMessage = localMessage;
    }
    
    String getLocalMessage() {
        return this.localMessage;
    }
}
