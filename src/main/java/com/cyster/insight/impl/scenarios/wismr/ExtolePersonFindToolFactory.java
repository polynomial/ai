package com.cyster.insight.impl.scenarios.wismr;

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

class PersonKey {
    private String keyType;
    private String keyValue;

    public PersonKey(@JsonProperty("keyType") String keyType, @JsonProperty("keyValue") String keyValue) {
        this.keyType = keyType;
        this.keyValue = keyValue;
    }

    @JsonProperty("keyType")
    public String getKeyType() {
        return keyType;
    }

    @JsonProperty("keyValue")
    public String getKeyValue() {
        return keyValue;
    }
}

class ExtolePersonFindTool implements ChatTool<PersonKey> {
    private final WebClient.Builder webClientBuilder;
    private Optional<String> accessToken;

    public ExtolePersonFindTool(WebClient.Builder builder, Optional<String> accessToken) {
        this.accessToken = accessToken;
        this.webClientBuilder = builder;
    }

    @Override
    public String getName() {
        return "person_find";
    }

    @Override
    public String getDescription() {
        return "Get the profile of a person given their person_id";
    }

    @Override
    public Class<PersonKey> getParameterClass() {
        return PersonKey.class;
    }

    @Override
    public Function<PersonKey, Object> getExecutor() {
        return parameter -> findPerson(parameter);
    }

    private JsonNode findPerson(PersonKey personKey) {
        var webClient = this.webClientBuilder.baseUrl("https://api.extole.io/v4/runtime-persons").build();

        if (accessToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"access_token_required\" }");
        }

        if (personKey.getKeyType() == null) {
            return toJsonNode("{ \"error\": \"person_key_type_not_specifid\" }");
        }

        var parameters = new LinkedMultiValueMap<String, String>();
        switch (personKey.getKeyType().toLowerCase()) {
        case "email":
            parameters.add("email", personKey.getKeyValue());
            break;
        case "partner_user_id":
            parameters.add("partner_user_id", personKey.getKeyValue());
            break;
        default:
            parameters.add("partner_id", personKey.getKeyType() + ":" + personKey.getKeyValue());
        }

        JsonNode jsonNode;
        try {
            jsonNode = webClient.get().uri(uriBuilder -> uriBuilder.queryParams(parameters).build())
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
public class ExtolePersonFindToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtolePersonFindToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    ExtolePersonFindTool create(Optional<String> accessToken) {
        return new ExtolePersonFindTool(this.webClientBuilder, accessToken);
    }
}
