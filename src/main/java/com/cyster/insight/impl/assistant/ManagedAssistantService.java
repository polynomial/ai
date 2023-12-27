package com.cyster.insight.impl.assistant;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.assistant.ManagedAssistant;
import com.cyster.insight.service.assistant.ManagedAssistantBuilder;
import com.cyster.insight.service.openai.OpenAiFactory;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.service.OpenAiService;


// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

@Component
public class ManagedAssistantService {
    private OpenAiService openAiService;
    
    // TODO add environment context
    public ManagedAssistantService(OpenAiFactory openAifactory) {
        this.openAiService = openAifactory.getService();
    }
    
    
    // scenario tbd
    // - how to define assistant
    // - perhaps like scenario, but is persistant, so need version, if change rebuilt
    //
    public Optional<ManagedAssistant> getAssistant(String name) {        
        var assistant = findAssistant(name);
        
        if (assistant.get().getMetadata().get("version") != "LATEST_VERSION") {
            assistant = Optional.empty();
        }
        
        
        return Optional.of(new ManagedAssistantImpl(this.openAiService, assistant.get()));
    }
    
    
    public ManagedAssistantBuilder createAssistant(String name) {
        return new ManagedAssistantBuilderImpl(this.openAiService, name);    
    }
    
    
    private Optional<Assistant> findAssistant(String name) {
        OpenAiResponse<Assistant> response = null;
        do {
            var searchBuilder = ListSearchParameters.builder().limit(99);
            if (response != null) {
                searchBuilder.after(response.getLastId());
            }
            response =  this.openAiService.listAssistants(searchBuilder.build());
            
            for (var assistant : response.getData()) {
                if (assistant.getName() != null && assistant.getName().equals(name)) { 
                    return Optional.of(assistant);
                }
            }
        } while (response.isHasMore());
        
        return Optional.empty();
    } 
}
