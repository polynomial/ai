package com.extole.sage.advisors.client;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

class ExtoleMeTool implements Tool<MyQUthorizationsRequest, ExtoleClientAdvisor.Context> {
    
    ExtoleMeTool() {
    }

    @Override
    public String getName() {
        return "extole_me";
    }

    @Override
    public String getDescription() {
        return "Gets your user_id and associated client_id";
    }

    @Override
    public Class<MyQUthorizationsRequest> getParameterClass() {
        return MyQUthorizationsRequest.class;
    }

    @Override
    public Object execute(MyQUthorizationsRequest request, ExtoleClientAdvisor.Context context) throws ToolException {     
        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(context.getUserAccessToken())
            .build();

        JsonNode resultNode;
        try {
            resultNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/me")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch(WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extole_token is invalid", exception); 
        } catch(WebClientException exception) {
            throw new ToolException("Internal tool error", exception); 
        }

        if (resultNode == null || !resultNode.isObject()) {
            return new ToolException(("Internal tool error, no results from request"));
        }
        
        return resultNode;
    }

}

class MyQUthorizationsRequest {
    @JsonPropertyDescription("Get more detailed information")
    @JsonProperty(required = false)
    public boolean extended;
}
