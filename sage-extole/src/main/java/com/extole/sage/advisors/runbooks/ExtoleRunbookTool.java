package com.extole.sage.advisors.runbooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.store.VectorStoreBuilderImpl;
import com.cyster.ai.weave.service.scenario.Id;
import com.cyster.ai.weave.service.scenario.VectorStore;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;
import com.extole.sage.scenarios.runbooks.RunbookScenario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtoleRunbookTool {
    
    private Id<VectorStore> id;
    
    public ExtoleRunbookTool(@Value("${OPENAI_API_KEY}") String openAiApiKey,
            List<RunbookScenario> runbookScenarios, ExtoleRunbookOther defaultRunbook) {        
        VectorStore.Builder builder = new VectorStoreBuilderImpl(openAiApiKey).withName("Runbooks"); 
        ObjectMapper objectMapper = new ObjectMapper();

        for(var runbook: runbookScenarios) {
            var book = new Runbook(runbook.getName(), runbook.getDescription(), runbook.getKeywords());
            String json;
            try {
                json = objectMapper.writeValueAsString(book);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to convert runbook to json");
            }
            builder.addDocument(runbook.getName() + ".json", json);
        }
        
        this.id = builder.create();
    }
    
    Id<VectorStore> getVectorStoreId() {
        return id;
    }
    
    public static class Runbook {
        private String name;
        private String description;
        private String keywords;
        
        public Runbook(String name, String description, String keywords) {
            this.name = name;
            this.description = description;
            this.keywords = keywords;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getDescription() {
            return this.description;    
        }
        
        public String getKeywords() {
            return this.keywords;
        }
    }

}



