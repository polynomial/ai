package com.cyster.insight.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.FunctionExecutor;

class ToolPojo<T> implements AdvisorTool<T> {
    private String name;
    private String description;
    private Class<T> parameterClass;
    private Function<T, Object> executor;

    public ToolPojo(String name, String description, Class<T> parameterClass, Function<T, Object> executor) {
        this.name = name;
        this.description = description;
        this.parameterClass = parameterClass;
        this.executor = executor;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Class<T> getParameterClass() {
        return this.parameterClass;
    }

    @Override
    public Function<T, Object> getExecutor() {
        return this.executor;
    }

}

public class Toolset {
    private FunctionExecutor functionExecutor;

    private Toolset(List<AdvisorTool<?>> tools) {
        var functions = new ArrayList<ChatFunction>();

        for (var tool : tools) {
            functions.add(chatTooltoChatFunction(tool));
        }

        this.functionExecutor = new FunctionExecutor(functions);
    }

    private static <T> ChatFunction chatTooltoChatFunction(AdvisorTool<T> tool) {
        return ChatFunction.builder()
            .name(tool.getName())
            .description(tool.getDescription())
            .executor(tool.getParameterClass(), tool.getExecutor())
            .build();
    }

    public List<ChatFunction> getFunctions() {
        return this.functionExecutor.getFunctions();
    }

    public ChatMessage call(ChatFunctionCall functionCall) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode result = objectMapper.valueToTree(functionExecutor.execute(functionCall));

        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting json node to json");
        }

        return new ChatMessage(ChatMessageRole.FUNCTION.value(), json, functionCall.getName());
    }

    public static class Builder {
        private List<AdvisorTool<?>> tools = new ArrayList<AdvisorTool<?>>();

        public <T> Builder addTool(String name, String description, Class<T> parameterClass,
            Function<T, Object> executor) {
            var tool = new ToolPojo<T>(name, description, parameterClass, executor);
            this.tools.add(tool);
            return this;
        }
        
        public <T> Builder addTool(AdvisorTool<T> tool) {
            this.tools.add(tool);

            return this;
        }

        public Toolset create() {
            return new Toolset(tools);
        }
    }
}