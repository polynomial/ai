package com.cyster.sherpa.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cyster.sherpa.impl.advisor.openai.OpenAiSchema;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;
import com.theokanning.openai.assistants.AssistantFunction;
import com.theokanning.openai.assistants.AssistantToolsEnum;

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

    public List<com.theokanning.openai.assistants.Tool> getAssistantTools() {
        var requestTools = new ArrayList<com.theokanning.openai.assistants.Tool>();

        if (this.retrieval) {
            requestTools.add(new com.theokanning.openai.assistants.Tool(AssistantToolsEnum.RETRIEVAL, null));
        }

        if (this.codeInterpreter) {
            requestTools.add(new com.theokanning.openai.assistants.Tool(AssistantToolsEnum.CODE_INTERPRETER, null));
        }

        for (var tool : this.toolset.getTools()) {

            var parameterSchema = getOpenAiToolParameterSchema(tool);

            AssistantFunction requestFunction = AssistantFunction.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .parameters(parameterSchema)
                .build();

            requestTools.add(new com.theokanning.openai.assistants.Tool(AssistantToolsEnum.FUNCTION, requestFunction));
        }

        return requestTools;
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

        return mapper.convertValue(schemaNode, new TypeReference<Map<String, Object>>() {
        });
    }

}
