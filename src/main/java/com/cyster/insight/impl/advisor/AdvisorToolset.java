package com.cyster.insight.impl.advisor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

class AdvisorToolset {
    private Toolset toolset;
    private boolean codeInterpreter = false;
    private boolean retrieval = false;
    
    AdvisorToolset(Toolset toolset) {
        this.toolset = toolset;    
    }
    
    public AdvisorToolset enableRetrival() {
        this.retrieval = true;
        
        return this;
    }
    
    public AdvisorToolset enableCodeInterpreter() {
        this.codeInterpreter = true;
        
        return this;
    }
    
    public List<Tool> getAssistantTools() {        
        List<Tool> requestTools = new ArrayList<Tool>();
        
        
        if (this.retrieval) {
          requestTools.add(new Tool(AssistantToolsEnum.RETRIEVAL, null));   
        }

        if (this.codeInterpreter) {
            requestTools.add(new Tool(AssistantToolsEnum.CODE_INTERPRETER, null));   
        }
        
        for(var tool : this.toolset.getAdvisorTools()) {
            
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
      
}
