package com.cyster.insight.impl.assistant;

import java.util.ArrayList;
import java.util.List;

import com.cyster.insight.impl.conversation.ChatTool;
import com.cyster.insight.service.assistant.ManagedAssistant;
import com.cyster.insight.service.assistant.ManagedAssistantBuilder;
import com.theokanning.openai.assistants.AssistantFunction;
import com.theokanning.openai.assistants.AssistantRequest;
import com.theokanning.openai.assistants.AssistantToolsEnum;
import com.theokanning.openai.assistants.Tool;
import com.theokanning.openai.service.OpenAiService;

public class ManagedAssistantBuilderImpl implements ManagedAssistantBuilder {

    private static final String MODEL = "gpt-4-1106-preview";
        
    private final OpenAiService openAiService;
    private final String name;
    private List<ChatTool<?>> tools = new ArrayList<ChatTool<?>>();
    
    ManagedAssistantBuilderImpl(OpenAiService openAiService, String name) {
        this.openAiService = openAiService;
        this.name = name;
    }
    
    @Override
    public  <T> ManagedAssistantBuilder withTool(ChatTool<T> tool) {
        tools.add(tool);
        return this;
    }

    //@Override
    //public ManagedAssistantBuilder withFile() {
        // TODO Auto-generated method stub
    //    return null;
    //}

    @Override
    public ManagedAssistant create() {
        
        List<Tool> requestTools = new ArrayList<Tool>();
        for(var tool : this.tools) {
            AssistantFunction requestFunction = AssistantFunction.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                //.parameters(tool.getParameterClass())
                .build();
                
            requestTools.add(new Tool(AssistantToolsEnum.FUNCTION, requestFunction)); 
        }
        
        var request = AssistantRequest.builder()
            .name(this.name)
            .model(MODEL)
            //.tools(requestTools)
            .build();
        
        var response =  this.openAiService.createAssistant(request);
        
        var assistant = new ManagedAssistantImpl(this.openAiService, response);

        return assistant;
    }

}
