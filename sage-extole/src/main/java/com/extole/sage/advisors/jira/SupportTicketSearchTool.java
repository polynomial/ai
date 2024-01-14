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

class SupportTicketSearchTool implements Tool<SupportTicketSearchRequest> {
    private Optional<String> jiraApiKey;
    private Optional<String> jiraBaseUrl;
    
    SupportTicketSearchTool(Optional<String> jiraApiKey, Optional<String> jiraBaseUrl) {
        this.jiraApiKey = jiraApiKey;
        this.jiraBaseUrl = jiraBaseUrl;
    }

    @Override
    public String getName() {
        return "ticket_search";
    }

    @Override
    public String getDescription() {
        return "Retrieve tickets from the extole Jira ticket tracking system";
    }

    @Override
    public Class<SupportTicketSearchRequest> getParameterClass() {
        return SupportTicketSearchRequest.class;
    }

    @Override
    public Object execute(SupportTicketSearchRequest searchRequest) {
        
        if (this.jiraApiKey.isEmpty()) {
            return toJsonNode("{ \"error\": \"jiraApiKey is required\" }");
        }
        if (this.jiraBaseUrl.isEmpty()) {
            return toJsonNode("{ \"error\": \"jiraBaseUrl is required\" }");
        }     
        
        String[] keyParts = this.jiraApiKey.get().split(":");
        if (keyParts.length != 2) {
            return toJsonNode("{ \"error\": \"jiraApiKey should be in the form email:secret\" }");       
        }
             
        var webClient = JiraWebClientBuilder.builder(this.jiraBaseUrl.get())
            .setApiKey(this.jiraApiKey.get())
            .build();
            
        String jql = "project = SUP";
        if (searchRequest.query != null && !searchRequest.query.isEmpty()) {
            jql = searchRequest.query;
        }
                
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
          payload.putArray("expand");

          ArrayNode fields = payload.putArray("fields");
          fields.add("summary");
          fields.add("status");
          fields.add("customfield_11312");
          fields.add("created");
          fields.add("updated");
          fields.add("priority");
          fields.add("parent");
          fields.add("assignee");
          fields.add("labels");
          payload.put("fieldsByKeys", false);
          payload.put("jql", jql);
          payload.put("maxResults", 15);
          payload.put("startAt", 0);
        }

        var resultNode = webClient.post()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search").build())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        JsonNode issuesNode = resultNode.path("issues");
        if (!issuesNode.isArray()) {
            return toJsonNode("{ \"error\": \"unexpected response\" }");
        }
        
        
        // TODO more robust pattern to refine result
        ObjectNode results = JsonNodeFactory.instance.objectNode();
        {
            ArrayNode tickets = results.putArray("tickets");
            int rowCount = 0;
            for (JsonNode issueNode : issuesNode) {
                ObjectNode ticket = tickets.addObject();
                ticket.put("number", issueNode.path("key").asText());
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
                rowCount++;
            }
            
            results.put("totalRowCount", resultNode.path("total").asInt());

            ObjectNode page = results.putObject("page");

            page.put("rowStart", resultNode.path("startAt").asInt());
            page.put("rowCount", rowCount);
            page.put("rowLimit", resultNode.path("maxResults").asInt());
        }
        
        return results;
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

class SupportTicketSearchRequest {
    @JsonPropertyDescription("JQL query for tickets, always prefix query with: project = SUP, if you have a clientId do a contains operation with the field name \"Client Id Calculated\"")
    @JsonProperty(required = false)
    public String query;

    @JsonPropertyDescription("The index of the first row in the result set to return, default 0")
    @JsonProperty(required = false)
    public int rowOffset;

    @JsonPropertyDescription("The maxminmum number of rows to return in one request, default 15")
    @JsonProperty(required = false)
    public int rowLimit;
}
