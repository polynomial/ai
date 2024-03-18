package com.extole.sage.advisors.runbooks;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.cyster.store.SimpleVectorStoreService;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;
import com.extole.sage.scenarios.runbooks.RunbookScenario;

@Component
public class RunbookStore {
    public static final String VECTOR_STORE_NAME = "runbooks";
    public static final String CHARSET_METADATA = "charset";
    public static final String SOURCE_METADATA = "source";
    
    private VectorStore runbookStore;
    private RunbookScenario defaultRunbook;
    
    /*
    private static final List<Runbook> runbooks = new ArrayList<>();
    static {
        runbooks.add(new Runbook("notifification-alert-monitoring", "triage client monitoring"));
        runbooks.add(new Runbook("notifification-alert-technical", "triage technical alert"));

        runbooks.add(new Runbook("notification-traffic-increase", "notification traffic increase automatic change percentage alerts")); - done
        runbooks.add(new Runbook("notification-traffic-decrease", "notification traffic decrease automatic change percentage alerts")); - done
        runbooks.add(new Runbook("notification-webhook", "notification webhook")); - done
        runbooks.add(new Runbook("notification-prehandler", "notification prehandler")); - done        
        runbooks.add(new Runbook("notification-email-render", "notification email render")); - done     
        runbooks.add(new Runbook("notification-other", "notification"));        
        runbooks.add(new Runbook("prehandler", "prehandler"));        
        runbooks.add(new Runbook("investigate-onsite-tags", "investigate tags zone onsite web"));
        runbooks.add(new Runbook("investigate-flow", "investigate flow"));
        runbooks.add(new Runbook("investigate-errors-reward", "investigate errors rewards gitft card"));
        runbooks.add(new Runbook("investigate-errors-dashboard", "investigate errors dashboard data discrepancies"));
        runbooks.add(new Runbook("creative-customization", "creative customization"));        
        runbooks.add(new Runbook("program-create", "program create new"));
        runbooks.add(new Runbook("data-fixup", "fixup data events bot container"));        
        runbooks.add(new Runbook(STORE_VERSION, "ai version test"));        
    }
*/
    
    public RunbookStore(SimpleVectorStoreService vectorStoreService,  List<RunbookScenario> runbookScenarios, ExtoleRunbookOther defaultRunbook) {
        this.runbookStore = vectorStoreService.getRepository(VECTOR_STORE_NAME);
        this.defaultRunbook = defaultRunbook;
        
        vectorStoreService.deleteRespository(VECTOR_STORE_NAME);
        this.runbookStore = vectorStoreService.getRepository(VECTOR_STORE_NAME);

        List<Document> documents = new ArrayList<>();
        for(var runbook: runbookScenarios) {            
            var metadata = new HashMap<String, Object>();
            metadata.put(SOURCE_METADATA, runbook.getName());
            metadata.put(CHARSET_METADATA, StandardCharsets.UTF_8.name());
                
            documents.add(new Document(runbook.getKeywords(), metadata));
        }
        this.runbookStore.add(documents);      
    }
    
    public List<Runbook> query(String query) {
        
        SearchRequest searchRequest = SearchRequest.query(query);
        searchRequest.withSimilarityThreshold(0.4);
                
        var documents = runbookStore.similaritySearch(searchRequest);
        
        if (documents.isEmpty()) {
            System.out.println("!!! Vector store - no match!");

            return List.of(new Runbook(defaultRunbook.getName(), defaultRunbook.getDescription()));
        }
        
        var results = new ArrayList<Runbook>();
        for (var document: documents) {
            System.out.println("!!! Vector Store - document: " + document.toString());
            
            results.add(new Runbook(document.getMetadata().get("source").toString(), document.getContent()));
        }
        
        return results; 
    }
    
    public static class Runbook {
        private String name;
        private String content;
        
        public Runbook(String name, String content) {
            this.name = name;
            this.content = content;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getContent() {
            return this.content;
        }
    }

}



