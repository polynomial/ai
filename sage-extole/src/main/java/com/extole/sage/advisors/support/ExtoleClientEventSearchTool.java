package com.extole.sage.advisors.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleClientEventSearchTool implements ExtoleSupportAdvisorTool<ExtoleClientEventSearchRequest> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleClientEventSearchTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
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

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_name", request.eventName);
        }

        var report = new ExtoleReportBuilder(this.extoleWebClientFactory.getWebClient(request.clientId));
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
