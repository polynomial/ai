package com.cyster.ai.weave.impl.advisor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cyster.ai.weave.impl.advisor.openai.OpenAiSchema;
import com.cyster.ai.weave.impl.store.SearchToolImpl;
import com.cyster.ai.weave.service.advisor.Tool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

import io.github.stefanbratanov.jvm.openai.CreateAssistantRequest;
import io.github.stefanbratanov.jvm.openai.Function;
import io.github.stefanbratanov.jvm.openai.ToolResources;
import io.github.stefanbratanov.jvm.openai.VectorStore;

class AdvisorToolset<C> {
    private Toolset<C> toolset;
    private boolean codeInterpreter = false;
    private boolean retrieval = false;
    
    AdvisorToolset(Toolset<C> toolset) {
        this.toolset = toolset;
    }

    public AdvisorToolset<C> enableRetrival() {
        this.retrieval = true;

        return this;
    }

    public AdvisorToolset<C> enableCodeInterpreter() {
        this.codeInterpreter = true;

        return this;
    }

    public void applyTools(CreateAssistantRequest.Builder requestBuilder) {
        if (this.retrieval) {
            requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.FileSearchTool());
        }

        if (this.codeInterpreter) {
            requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.CodeInterpreterTool());
        }

        for (var tool : this.toolset.getTools()) {
            if (tool.getName().equals(SearchToolImpl.NAME)) {
                requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.FileSearchTool());
                
                // TODO add type, create tools from AdvisorService, base type apply()
                var searchTool = (SearchToolImpl<C>)tool; 
                
                List<String> ids =searchTool.getVectorStores().stream()
                    .map(VectorStore::id)
                    .collect(Collectors.toList());
                
                var toolResources = ToolResources.fileSearchToolResources(ids.toArray(new String[0]));

                requestBuilder.toolResources(toolResources);
            } else {
                var parameterSchema = getOpenAiToolParameterSchema(tool);
    
                var requestFunction = Function.newBuilder()
                    .name(tool.getName())
                    .description(tool.getDescription())
                    .parameters(parameterSchema)
                    .build();
    
                requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.FunctionTool(requestFunction));
            }
        }
    }

    private static <C> JsonSchema getToolParameterSchema(Tool<?, C> tool) {
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

    private static <C> Map<String, Object> getOpenAiToolParameterSchema(Tool<?, C> tool) {
        ObjectMapper mapper = new ObjectMapper();

        var schema = new OpenAiSchema(getToolParameterSchema(tool));

        var schemaNode = schema.toJsonNode();

        return mapper.convertValue(schemaNode, new TypeReference<Map<String, Object>>() {});
    }

}
