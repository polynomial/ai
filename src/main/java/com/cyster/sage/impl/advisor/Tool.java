package com.cyster.sage.impl.advisor;


public interface Tool<T> {
    
    String getName();

    String getDescription();

    Class<T> getParameterClass();
    
    Object execute(T parameters);
}
