package com.extole.sage.advisors.jira;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.http.MediaType;

import com.cyster.sherpa.impl.advisor.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class SupportTicketGetTool implements Tool<SupportTicketGetRequest> {
    private Optional<String> jiraApiKey;
    private Optional<String> jiraBaseUrl;
    
    SupportTicketGetTool(Optional<String> jiraApiKey, Optional<String> jiraBaseUrl) {
        this.jiraApiKey = jiraApiKey;
        this.jiraBaseUrl = jiraBaseUrl;
    }

    @Override
    public String getName() {
        return "ticket_get";
    }

    @Override
    public String getDescription() {
        return "Retrieve tickets from the extole Jira support ticket tracking system";
    }

    @Override
    public Class<SupportTicketGetRequest> getParameterClass() {
        return SupportTicketGetRequest.class;
    }

    @Override
    public Object execute(SupportTicketGetRequest searchRequest) {
        
        if (this.jiraApiKey.isEmpty()) {
            return toJsonNode("{ \"error\": \"jiraApiKey is required\" }");
        }
        if (this.jiraBaseUrl.isEmpty()) {
            return toJsonNode("{ \"error\": \"jiraBaseUrl is required\" }");
        }     
             
        var webClient = JiraWebClientBuilder.builder(this.jiraBaseUrl.get())
            .setApiKey(this.jiraApiKey.get())
            .build();
            
        if (searchRequest.key != null && searchRequest.key.isEmpty()) {
            return toJsonNode("{ \"error\": \"no ticket key specified\" }");
        }
                
        var resultNode = webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + searchRequest.key).build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();
                
        var issueNode = resultNode;
        
        // TODO more robust pattern to refine result
        ObjectNode ticket = JsonNodeFactory.instance.objectNode();
        {
            ticket.put("key", issueNode.path("key").asText());
            ticket.put("summary", issueNode.path("fields").path("summary").asText());
          
            JsonNode assignee = issueNode.path("fields").path("assignee");
            if (!assignee.isMissingNode()) {
                ticket.put("assignee", assignee.path("emailAddress").asText());
            } else {
                ticket.putNull("assignee"); 
            }
                
            JsonNode status = issueNode.path("fields").path("status");
            if (!status.isMissingNode()) {
                ticket.put("status", status.path("name").asText());
            } else {
                ticket.putNull("status");
            }
                
            JsonNode parent = issueNode.path("fields").path("parent");
            if (!parent.isMissingNode()) {
                ticket.put("classification", parent.path("fields").path("summary").asText());
            } else {
                ticket.putNull("classification");
            }

            JsonNode customField11312 = issueNode.path("fields").path("customfield_11312");
            if (!customField11312.isMissingNode()) {
                ticket.put("client", customField11312.path("value").asText());
            } else {
                ticket.putNull("client");
            }
                
            ticket.put("createdDate", issueNode.path("fields").path("created").asText());
            ticket.put("updatedDate", issueNode.path("fields").path("updated").asText());

            Iterator<JsonNode> labelsNode = issueNode.path("fields").path("labels").elements();
            ArrayNode labels = ticket.putArray("labels");
            while (labelsNode.hasNext()) {
                JsonNode label = labelsNode.next();
                labels.add(label.asText());
            }

            JsonNode descriptionNode = issueNode.path("fields").path("description");
            if (descriptionNode.isMissingNode()) {
                ticket.putNull("description");
            } else {
                String content = "TESTING 1";
                Iterator<JsonNode> contentNodes = descriptionNode.path("content").elements();
                while (contentNodes.hasNext()) {
                    content = content + " 2";
                    JsonNode contentNode = contentNodes.next();
                    if (contentNode.path("type").asText().equals("paragraph")) {
                        content = content + " 3";
                        var subContentNodes = contentNode.path("content").elements();
                        while (subContentNodes.hasNext()) {
                            var subContentNode = subContentNodes.next();
                            if (subContentNode.path("type").asText().equals("text")) {
                                content = content + subContentNode.path("text").asText() + "\n";
                            }
                        }
                    }
                }
                ticket.put("description", content);
            }
        }
        
        return ticket;
    }

    private static JsonNode toJsonNode(String json) {
        JsonNode jsonNode;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Unable to parse Json response", exception);
        }
        return jsonNode;
    }
}

class SupportTicketGetRequest {
    @JsonPropertyDescription("ticket key")
    @JsonProperty(required = true)
    public String key;
}