package com.extole.sage.advisors.runbooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.store.SearchToolBuilderImpl;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.SearchTool;
import com.cyster.ai.weave.service.scenario.Id;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;
import com.extole.sage.scenarios.runbooks.RunbookScenario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtoleRunbookToolFactory {
    
    private SearchTool<Void> searchTool;
    
    public ExtoleRunbookToolFactory(AdvisorService advisorService, 
            List<RunbookScenario> runbookScenarios, ExtoleRunbookOther defaultRunbook) {
        ObjectMapper objectMapper = new ObjectMapper();

        SearchTool.Builder<Void> builder = advisorService.searchToolBuilder();
        builder.withName("runbooks");
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
        
        this.searchTool = builder.create();
    }
    
    public SearchTool<Void> getRunbookSearchTool() {
        return this.searchTool;
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



