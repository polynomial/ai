package com.cyster.sherpa.impl.advisor;


public interface Tool<T> {
    
    String getName();

    String getDescription();

    Class<T> getParameterClass();
    
    Object execute(T parameters) throws ToolException;
}
