package com.cyster.sherpa.impl.advisor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; 

public class Toolset<C> {
    private Map<String, Tool<?, C>> tools = new HashMap<String, Tool<?, C>>();
    
    private Toolset(List<Tool<?, C>> tools) {        
        for(var tool: tools) {
            this.tools.put(tool.getName(), tool);
        }       
    }

    public String execute(String name, String jsonParameters, C context) {
        ObjectMapper mapper = new ObjectMapper();
        
        if (!tools.containsKey(name)) {
            return new ToolError("No tool called: " + name, false).toJsonString();
        }
        Tool<?, C> tool = tools.get(name);
        
        try {
            var result = executeTool(tool, jsonParameters, context);

            return mapper.writeValueAsString(result);
        } catch (FatalToolException exception) {
            return new ToolError(exception.getMessage(), true).toJsonString();
        } catch (ToolException exception) {
            return new ToolError(exception.getMessage(), false).toJsonString();
        } catch (JsonProcessingException e) {
            return new ToolError("Tool result could not be formated as json", false).toJsonString();
        }
    }

    public <T> Object executeTool(Tool<T, C> tool, String jsonArguments, C context) throws ToolException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            T parameters = mapper.readValue(jsonArguments, tool.getParameterClass());                        
            return tool.execute(parameters, context); 
        } catch (JsonProcessingException e) {
            return new ToolError("Tool parameters did not match json schema", false).toJsonString();
        }
    }
     
    public Collection<Tool<?, C>> getTools() {
        return this.tools.values();
    }
    
    public static class Builder<C> {
        private List<Tool<?, C>> tools = new ArrayList<Tool<?, C>>();

        public <T> Builder<C> addTool(Tool<T, C> tool) {
            this.tools.add(tool);

            return this;
        }

        public Toolset<C> create() {
            return new Toolset<C>(tools);
        }
    }  
}