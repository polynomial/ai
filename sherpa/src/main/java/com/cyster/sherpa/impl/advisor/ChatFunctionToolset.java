package com.cyster.sherpa.impl.advisor;

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

public class ChatFunctionToolset<C> {
    private Toolset<C> toolset;
    private FunctionExecutor functionExecutor;
    C context = null;
    
    public ChatFunctionToolset(Toolset<C> toolset) {
        this.toolset = toolset;
        
        var functions = new ArrayList<ChatFunction>();
        for(var tool: this.toolset.getTools()) {
            functions.add(chatTooltoChatFunction(tool));
        }
         this.functionExecutor = new FunctionExecutor(functions);
    }
    
    public ChatFunctionToolset<C> withContext(C context) {
        this.context = context;
        return this;
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
    
    private <T> ChatFunction chatTooltoChatFunction(Tool<T, C> tool) {
        return ChatFunction.builder()
            .name(tool.getName())
            .description(tool.getDescription())
            .executor(tool.getParameterClass(), parameters -> {
                try {
                    return tool.execute(parameters, ChatFunctionToolset.this.context);
                } catch (ToolException exception) {
                    return new ToolError(exception.getMessage(), false).toString();
                }
            })
            .build();
    }
}
