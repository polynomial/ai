package com.extole.sage.advisors.support;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

class ExtoleClientSearchTool implements Tool<ClientSearchRequest> {
    private Optional<String> extoleSuperUserToken;
    
    ExtoleClientSearchTool(Optional<String> extoleSuperUserToken) {
        this.extoleSuperUserToken = extoleSuperUserToken;
    }

    @Override
    public String getName() {
        return "extole_client_search";
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
    public Object execute(ClientSearchRequest searchRequest) throws ToolException {
        
        if (this.extoleSuperUserToken.isEmpty()) {
            throw new FatalToolException("extoleSuperUserToken is required");
        }

        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(this.extoleSuperUserToken.get())
            .build();

        JsonNode resultNode;
        try {
            resultNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/clients")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch(WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception); 
        } catch(WebClientException exception) {
            System.out.println("Exception: " + exception.toString());
            throw exception;
        }

        if (resultNode == null || !resultNode.isArray()) {
            return new ToolException(("Query failed with unexpected result"));
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

}

class ClientSearchRequest {
    @JsonPropertyDescription("Query client list against client name, client short_name or client_id")
    @JsonProperty(required = false)
    public String query;

}
