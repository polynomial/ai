package com.cyster.insight.impl.conversation;

import java.util.function.Function;

public interface ChatTool<T> {
    String getName();

    String getDescription();

    Class<T> getParameterClass();

    Function<T, Object> getExecutor();
}
