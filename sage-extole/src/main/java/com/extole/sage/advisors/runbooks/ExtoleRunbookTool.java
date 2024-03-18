package com.extole.sage.advisors.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.runbooks.ExtoleRunbookTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class ExtoleRunbookTool  implements Tool<Request, Void> {
    public static final String VECTOR_STORE_NAME = "runbooks";
    public static final String CHARSET_METADATA = "charset";
    public static final String SOURCE_METADATA = "source";
    
    private RunbookStore runbookStore;

    public ExtoleRunbookTool(RunbookStore runbookStore) {
        this.runbookStore = runbookStore;
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
        return runbookStore.query(request.query_vector);
    }
    
    static class Request {
        // TODO description of how to normalize the query doesn't seem to have any effect here (currently put in Advisor where it works)
        @JsonPropertyDescription("Query used to find the best runbooks. Please:"
                + "correct grammar, "
                + "remove duplicate words, "
                + "remove PII, remove URLs, remove company names, "
                + "remove stop words (common words like \"this\", \"is\", \"in\", \"by\", \"with\" etc), "
                + "normalize the text (convert to lowercase and remove special characters) "
                + "and keep to 20 words or less.") 
        @JsonProperty(required = true)
        public String query_vector;
    }
}



