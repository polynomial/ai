package com.cyster.insight.impl.advisor;

import java.util.function.Function;

public interface AdvisorTool<T> {
    String getName();

    String getDescription();

    Class<T> getParameterClass();

    Function<T, Object> getExecutor();
    
    Object execute(Object parameters);
}
