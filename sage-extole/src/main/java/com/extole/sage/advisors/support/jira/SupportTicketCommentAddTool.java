package com.extole.sage.advisors.support.jira;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.support.ExtoleSupportAdvisorTool;
import com.extole.sage.advisors.support.jira.SupportTicketCommentAddTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class SupportTicketCommentAddTool implements ExtoleSupportAdvisorTool<Request> {
    private JiraWebClientFactory jiraWebClientFactory;
    
    SupportTicketCommentAddTool(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    @Override
    public String getName() {
        return "ticket_comment_add";
    }

    @Override
    public String getDescription() {
        return "Post a comment to a Jira support ticket";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {        
        if (request.key == null || request.key.isEmpty()) {
            throw new FatalToolException("Attribute ticket key not specified");
        }
            
        if (request.comment == null || request.comment.isEmpty()) {
            throw new FatalToolException("Attribute comment must be specified");
        }
        
        JsonNode adf;
        var objectMapper = new ObjectMapper();
        try {
            adf = objectMapper.readTree(request.comment);   // TODO validate
        } catch (JsonProcessingException exception) {
            throw new FatalToolException("comment attribute could not be interprested as ADF in a JSON format", exception);
        }
        
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            payload.set("body", adf);      
        }
                
        var result =  this.jiraWebClientFactory.getWebClient().post()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + request.key + "/comment").build())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();
                
        if (result == null || !result.has("id")) {
            throw new ToolException("Jira comment failed, unexpected response");
        }
        
        return result;  
    }

    static class Request {
        @JsonPropertyDescription("ticket key")
        @JsonProperty(required = true)
        public String key;
        
        @JsonPropertyDescription("escaped string of the Json comment in Jira's ADF format")
        @JsonProperty(required = true)
        public String comment;        
    }
}



