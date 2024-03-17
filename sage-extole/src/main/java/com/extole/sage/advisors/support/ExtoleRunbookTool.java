package com.extole.sage.advisors.support;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.ToolException;
import com.cyster.store.VectorStoreService;
import com.extole.sage.advisors.support.ExtoleRunbookTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class ExtoleRunbookTool  implements ExtoleSupportAdvisorTool<Request>  {
    public static final String VECTOR_STORE_NAME = "runbooks";
    public static final String CHARSET_METADATA = "charset";
    public static final String SOURCE_METADATA = "source";
    
    private VectorStore runbookStore;

    private static final List<Runbook> runbooks = new ArrayList<>();
    static {
        runbooks.add(new Runbook("notification-traffic-increase", "notification traffic increase change percentage alerts"));
        runbooks.add(new Runbook("notification-traffic-decrease", "notification traffic decrease change percentage alerts"));
        runbooks.add(new Runbook("notification-webhook", "notification webhook"));
        runbooks.add(new Runbook("notification-prehandler", "notification prehandler"));        
        runbooks.add(new Runbook("notification-email-render", "notification email render"));        
        runbooks.add(new Runbook("notification-other", "notification"));        
        runbooks.add(new Runbook("prehandler", "prehandler"));        
        runbooks.add(new Runbook("wismr", "reward gift card"));        
        runbooks.add(new Runbook("ai-test", "ai test"));        
    }

    public ExtoleRunbookTool(VectorStoreService vectorStoreService) {
        this.runbookStore = vectorStoreService.getRepository(VECTOR_STORE_NAME);
        if (runbookStore.similaritySearch("ai test").isEmpty()) {
            List<Document> documents = new ArrayList<>();
            for(var runbook: runbooks) {
                var metadata = new HashMap<String, Object>();
                metadata.put(SOURCE_METADATA, runbook.getName());
                metadata.put(CHARSET_METADATA, StandardCharsets.UTF_8.name());
                    
                documents.add(new Document(runbook.getContent(), metadata));
            }
            this.runbookStore.add(documents);      
        }
    }
    
    @Override
    public String getName() {
        return "extole_runbook";
    }

    @Override
    public String getDescription() {
        return "Finds the best runbook given a problem description";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        var documents = runbookStore.similaritySearch(request.query_vector);
        
        if (documents.isEmpty()) {
            return "unclassified";
        }
        
        return documents.get(0).getMetadata().get("source"); 
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
    
    static class Request {
        @JsonPropertyDescription("The query used against a vector store. Please:"
                + "correct grammar, "
                + "remove duplicate words, "
                + "remove PII and URLs, "
                + "remove stop words (common words like \"this\", \"is\", \"in\", \"by\", \"with\" etc), "
                + "normalize the text (convert to lowercase and remove special characters) "
                + "and keep to 20 words or less.") 
        @JsonProperty(required = true)
        public String query_vector;
    }
}



