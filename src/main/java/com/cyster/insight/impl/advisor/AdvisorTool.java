package com.cyster.insight.impl.advisor;

public interface AdvisorTool<T> {
    String getName();

    String getDescription();

    Class<T> getParameterClass();
    
    Object execute(T parameters);
}
