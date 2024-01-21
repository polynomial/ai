package com.cyster.sherpa.impl.advisor;

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
            return new ToolError("No tool called: " + name, false).toJsonString();
        }
        Tool<?> tool = tools.get(name);
        
        try {
            var result = executeTool(tool, jsonParameters);
            return mapper.writeValueAsString(result);
        } catch (FatalToolException exception) {
            return new ToolError(exception.getMessage(), true).toJsonString();
        } catch (ToolException exception) {
            return new ToolError(exception.getMessage(), false).toJsonString();
        } catch (JsonProcessingException e) {
            return new ToolError("Tool result could not be formated as json", false).toJsonString();
        }
    }

    public <T> Object executeTool(Tool<T> tool, String jsonArguments) throws ToolException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            T parameters = mapper.readValue(jsonArguments, tool.getParameterClass());
            return tool.execute(parameters);
        } catch (JsonProcessingException e) {
            return new ToolError("Tool parameters did not match json schema", false).toJsonString();
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