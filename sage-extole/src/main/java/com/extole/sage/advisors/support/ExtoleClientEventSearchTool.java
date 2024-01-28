package com.extole.sage.advisors.support;

import java.util.Optional;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ExtoleClientEventSearchTool implements Tool<ExtoleClientEventSearchRequest, Void> {
    private Optional<String> extoleSuperUserToken;

    ExtoleClientEventSearchTool(Optional<String> extoleSuperUserToken) {
        this.extoleSuperUserToken = extoleSuperUserToken;
    }

    @Override
    public String getName() {
        return "extole_client_event_search";
    }

    @Override
    public String getDescription() {
        return "Finds client events by event name or user";
    }

    @Override
    public Class<ExtoleClientEventSearchRequest> getParameterClass() {
        return ExtoleClientEventSearchRequest.class;
    }

    @Override
    public Object execute(ExtoleClientEventSearchRequest request, Void context) throws ToolException {

        if (this.extoleSuperUserToken.isEmpty()) {
            throw new FatalToolException("extoleSuperUserToken is required");
        }

        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setSuperApiKey(this.extoleSuperUserToken.get())
            .setClientId(request.clientId)
            .build();

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_name", request.eventName);
        }

        var report = new ExtoleReportBuilder(webClient);
        report.withName("client_events");
        report.withDisplayName("Client Events - " + request.eventName);
        report.withParameters(parameters);

        return report.build();
    }

}

class ExtoleClientEventSearchRequest {
    @JsonProperty(required = true)
    public String clientId;

    @JsonPropertyDescription("Query client events by event name")
    @JsonProperty(required = false)
    public String eventName;

}
