package com.cyster.insight.impl.scenarios.wismr;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.insight.impl.conversation.ChatTool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ExtolePersonRewardToolParameters {
    private String personId;

    public ExtolePersonRewardToolParameters(@JsonProperty("personId") String personId) {
        this.personId = personId;
    }

    @JsonProperty("personId")
    public String getPersonId() {
        return this.personId;
    }
}

class ExtolePersonRewardsTool implements ChatTool<ExtolePersonRewardToolParameters> {
    private final WebClient.Builder webClientBuilder;
    private Optional<String> accessToken;

    public ExtolePersonRewardsTool(WebClient.Builder builder, Optional<String> accessToken) {
        this.accessToken = accessToken;
        this.webClientBuilder = builder;
    }

    @Override
    public String getName() {
        return "person_rewards";
    }

    @Override
    public String getDescription() {
        return "Get a persons reward given their profile_id";
    }

    @Override
    public Class<ExtolePersonRewardToolParameters> getParameterClass() {
        return ExtolePersonRewardToolParameters.class;
    }

    @Override
    public Function<ExtolePersonRewardToolParameters, Object> getExecutor() {
        return parameter -> loadRewards(parameter);
    }

    private JsonNode loadRewards(ExtolePersonRewardToolParameters personHandle) {
        var webClient = this.webClientBuilder.baseUrl("https://api.extole.io/v4/runtime-persons/{personId}/rewards")
            .build();

        if (accessToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"access_token_required\" }");
        }

        if (personHandle.getPersonId() == null || personHandle.getPersonId().isBlank()) {
            return toJsonNode("{ \"error\": \"person_id_not_specifid\" }");
        }

        var parameters = new LinkedMultiValueMap<String, String>();

        var pathParameters = new HashMap<String, String>();
        pathParameters.put("personId", personHandle.getPersonId());

        JsonNode jsonNode;
        try {
            System.out.println("Parameters: " + parameters);

            jsonNode = webClient.get().uri(uriBuilder -> uriBuilder.queryParams(parameters).build(pathParameters))
                .header("Authorization", "Bearer " + this.accessToken.get())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode().value() == 403) {
                return toJsonNode("{ \"error\": \"access_denied\" }");
            } else if (exception.getStatusCode().is4xxClientError()) {
                return toJsonNode("{ \"error\": \"bad_request\" }");
            } else {
                return toJsonNode("{ \"error\": \"something_went_wrong\" }");
            }
        }

        return jsonNode;
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

@Component
public class ExtolePersonRewardsToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtolePersonRewardsToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    ExtolePersonRewardsTool create(Optional<String> accessToken) {
        return new ExtolePersonRewardsTool(this.webClientBuilder, accessToken);
    }
}
