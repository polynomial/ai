package com.cyster.sage.impl.advisor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; 

public class Toolset {
    private Map<String, Tool<?>> tools = new HashMap<String, Tool<?>>();
    
    private Toolset(List<Tool<?>> tools) {        
        for(var tool: tools) {
            this.tools.put(tool.getName(), tool);
        }       
    }

    public String execute(String name, String jsonParameters) {
        ObjectMapper mapper = new ObjectMapper();
        
        if (!tools.containsKey(name)) {
            throw new RuntimeException("No tool called: " + name);
        }
        Tool<?> tool = tools.get(name);
        
        try {
            var result = executeTool(tool, jsonParameters);
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Tool bad call result", e);
        }
    }

    public <T> Object executeTool(Tool<T> tool, String jsonArguments) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            T parameters = mapper.readValue(jsonArguments, tool.getParameterClass());
            return tool.execute(parameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Tool bad call parmeters", e);
        }
    }
     
    public Collection<Tool<?>> getTools() {
        return this.tools.values();
    }
    
    public static class Builder {
        private List<Tool<?>> tools = new ArrayList<Tool<?>>();

        public <T> Builder addTool(Tool<T> tool) {
            this.tools.add(tool);

            return this;
        }

        public Toolset create() {
            return new Toolset(tools);
        }
    }  
}