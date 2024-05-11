package com.cyster.assistant.impl.advisor;


public interface Tool<T, C> {
    
    String getName();

    String getDescription();

    Class<T> getParameterClass();
    
    Object execute(T parameters, C context) throws ToolException;
}
