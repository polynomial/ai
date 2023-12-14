package com.cyster.insight.impl.scenarios.report;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.insight.impl.conversation.ChatTool;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ReportHandle {
    @JsonPropertyDescription("The id of the report to load")
    public String id;
}

class ExtoleReportConfigurationTool implements ChatTool<ReportHandle> {
    private final WebClient.Builder webClientBuilder;
    private Optional<String> accessToken;

    public ExtoleReportConfigurationTool(WebClient.Builder builder, Optional<String> accessToken) {
        this.accessToken = accessToken;
        this.webClientBuilder = builder;
    }

    @Override
    public String getName() {
        return "get_report_configuration";
    }

    @Override
    public String getDescription() {
        return "Get the configuration of a report given the report_id";
    }

    @Override
    public Class<ReportHandle> getParameterClass() {
        return ReportHandle.class;
    }

    @Override
    public Function<ReportHandle, Object> getExecutor() {
        return reportHandle -> reportConfigurationLoader(reportHandle);
    }

    private JsonNode reportConfigurationLoader(ReportHandle reportHandle) {
        var webClient = this.webClientBuilder.baseUrl("https://api.extole.io/v4/reports").build();

        if (accessToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"access_token_required\" }");
        }

        JsonNode jsonNode;
        try {
            jsonNode = webClient.get().uri("/{id}", reportHandle.id)
                .header("Authorization", "Bearer " + this.accessToken.get())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ObjectNode.class)
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
public class ExtoleReportConfigurationToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtoleReportConfigurationToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    ExtoleReportConfigurationTool create(Optional<String> accessToken) {
        return new ExtoleReportConfigurationTool(this.webClientBuilder, accessToken);
    }
}
