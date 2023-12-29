package com.cyster.insight.impl.advisor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;
import com.theokanning.openai.assistants.AssistantFunction;
import com.theokanning.openai.assistants.AssistantToolsEnum;
import com.theokanning.openai.assistants.Tool;
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

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Object parameters) {
        return this.getExecutor().apply((T)parameters);   
    }
    
    @Override
    public Function<T, Object> getExecutor() {
        return this.executor;
    }
}

public class Toolset {
    private Map<String, AdvisorTool<?>> tools = new HashMap<String, AdvisorTool<?>>();
    private FunctionExecutor functionExecutor;
    
    private Toolset(List<AdvisorTool<?>> tools) {
        var functions = new ArrayList<ChatFunction>();
        
        for(var tool: tools) {
            this.tools.put(tool.getName(), tool);
            functions.add(chatTooltoChatFunction(tool));
        }
       
        this.functionExecutor = new FunctionExecutor(functions);
    }

    public String execute(String name, String jsonArguments) {
        ObjectMapper mapper = new ObjectMapper();
        
        AdvisorTool<?> tool = tools.get(name);
        
        try {
            var parameters = mapper.readValue(jsonArguments, tool.getParameterClass());
            var result = tool.execute(parameters);
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("bad call", e);
        }
    }
    
    private static <T> ChatFunction chatTooltoChatFunction(AdvisorTool<T> tool) {
        return ChatFunction.builder()
            .name(tool.getName())
            .description(tool.getDescription())
            .executor(tool.getParameterClass(), tool.getExecutor())
            .build();
    }

    private static JsonSchema getToolParameterSchema(AdvisorTool<?> tool) {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(mapper);
        
        JsonSchema parameterSchema;
        try {
            parameterSchema = schemaGenerator.generateSchema(tool.getParameterClass());
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } 
   
        return parameterSchema;
    }

    private static ObjectNode getToolParameterSchemaAsJsonObjectNode(AdvisorTool<?> tool) {
        ObjectMapper mapper = new ObjectMapper();

        JsonSchema schema = getToolParameterSchema(tool);
             
        ObjectNode schemaNode = mapper.valueToTree(schema);
        
        if (!schemaNode.isObject()) {
            throw new RuntimeException("Expected an object");
        }
       
        return schemaNode;
    }
    
    private static Map<String, Object> getOpenAiToolParameterSchema(AdvisorTool<?> tool) {
        ObjectMapper mapper = new ObjectMapper();

        var schemaNode = getToolParameterSchemaAsJsonObjectNode(tool);
        
        if (!schemaNode.path("id").isMissingNode()) {
            schemaNode.remove("id");
        }

        ArrayNode requiredNode = mapper.createArrayNode();

        JsonNode propertiesNode = schemaNode.path("properties");
        if (propertiesNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();
                
                if (fieldValue.isObject()) {
                    var fieldObject = (ObjectNode)fieldValue;
       
                    if (!fieldObject.path("required").isMissingNode()) {
                        fieldObject.remove("required");
                        requiredNode.add(fieldName);
                    }
                }
            }
            schemaNode.set("required", requiredNode);
        }
     
        return mapper.convertValue(schemaNode, new TypeReference<Map<String, Object>>() {});
    }
     
    public Collection<AdvisorTool<?>> getAdvisorTools() {
        return this.tools.values();
    }
    
    public List<ChatFunction> getFunctions() {
        return this.functionExecutor.getFunctions();
    }

    
    public List<Tool> getAssistantTools() {        
        List<Tool> requestTools = new ArrayList<Tool>();
        for(var tool : this.tools.values()) {
            
            var parameterSchema = getOpenAiToolParameterSchema(tool);
                        
            System.out.println("SchemaMap: " + parameterSchema.toString());
                        
            AssistantFunction requestFunction = AssistantFunction.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .parameters(parameterSchema)
                .build();
                
            requestTools.add(new Tool(AssistantToolsEnum.FUNCTION, requestFunction)); 
        }
        
        return requestTools;
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