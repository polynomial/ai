package com.cyster.sage.impl.advisor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.FunctionExecutor;

public class ChatFunctionToolset {
    private Toolset toolset;
    private FunctionExecutor functionExecutor;
    
    public ChatFunctionToolset(Toolset toolset) {
        this.toolset = toolset;
        
        var functions = new ArrayList<ChatFunction>();
        for(var tool: this.toolset.getTools()) {
            functions.add(chatTooltoChatFunction(tool));
        }
         this.functionExecutor = new FunctionExecutor(functions);
    }
    
    public List<ChatFunction> getFunctions() {
        return this.functionExecutor.getFunctions();
    }

    public ChatMessage call(ChatFunctionCall functionCall) {        
        ObjectMapper objectMapper = new ObjectMapper();
        
        JsonNode result = objectMapper.valueToTree(this.functionExecutor.execute(functionCall));

        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting json node to json");
        }

        return new ChatMessage(ChatMessageRole.FUNCTION.value(), json, functionCall.getName());
    }
    
    private static <T> ChatFunction chatTooltoChatFunction(Tool<T> tool) {
        return ChatFunction.builder()
            .name(tool.getName())
            .description(tool.getDescription())
            .executor(tool.getParameterClass(), parameters -> tool.execute(parameters))
            .build();
    }
}
