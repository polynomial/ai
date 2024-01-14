package com.extole.sage.advisors.jira;

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

class ClientSearchTool implements Tool<ClientSearchRequest> {
    private Optional<String> extoleSuperUserToken;
    
    ClientSearchTool(Optional<String> extoleSuperUserToken) {
        this.extoleSuperUserToken = extoleSuperUserToken;
    }

    @Override
    public String getName() {
        return "client_search";
    }

    @Override
    public String getDescription() {
        return "Finds an Extole client by client name, clientShortName or clientId";
    }

    @Override
    public Class<ClientSearchRequest> getParameterClass() {
        return ClientSearchRequest.class;
    }

    @Override
    public Object execute(ClientSearchRequest searchRequest) {
        
        if (this.extoleSuperUserToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"extoleSuperUserToken is required\" }");
        }

        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(this.extoleSuperUserToken.get())
            .build();

        var resultNode = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/clients")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (!resultNode.isArray()) {
            return toJsonNode("{ \"error\": \"unexpected response\" }");
        }
        
        if (searchRequest.query == null || searchRequest.query.isEmpty()) {
            return resultNode;
        }
        var query = searchRequest.query.toLowerCase();
        
        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        {          
            for (JsonNode clientNode : resultNode) {
                if (clientNode.path("name").asText().toLowerCase().contains(query) ||
                    clientNode.path("short_name").asText().toLowerCase().contains(query) ||
                    clientNode.path("client_id").asText().equals(query)) {
                    results.add(clientNode);
                }   
            }
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

class ClientSearchRequest {
    @JsonPropertyDescription("Query client list against client name, client short_name or client_id")
    @JsonProperty(required = false)
    public String query;

}
